package top.trister.shipper.core.task;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.trister.shipper.core.api.*;
import top.trister.shipper.core.dsl.DSLDelegate;
import top.trister.shipper.core.dsl.HandlerDefinition;
import top.trister.shipper.core.exception.MultipleException;
import top.trister.shipper.core.exception.ShipperException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static top.trister.shipper.core.task.TaskStepEnum.*;

@Data
public abstract class AbstractShipperTask implements ShipperTask, AutoCloseable, ShipperTaskContextAware, LogAware {

    protected Logger log = LoggerFactory.getLogger(AbstractShipperTask.class);

    /**
     * 任务的描述信息
     * 子类需要保证只有一个线程进行修改
     */
    protected volatile ShipperTaskContext shipperTaskContext;
    /**
     * 当前任务所处的状态
     * 子类需要保证只有一个线程进行修改
     */
    protected volatile TaskStepEnum step = CREATE;

    /**
     * 封闭在线程内部的events对象
     * 子类需要保证其仅被一个线程修改
     */
    protected volatile List<Map> events;

    /**
     * 异常内容
     * 子类需要保证其内容仅被一个线程修改
     */
    protected final List<Exception> exceptions = new ArrayList<>();


    @Override
    public void shipperTaskContext(ShipperTaskContext shipperTaskContext) {
        this.shipperTaskContext = shipperTaskContext;
    }

    @Override
    public List<Exception> exceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    @Override
    public TaskStepEnum state() {
        return step;
    }

    @Override
    public List<Map> nowEvents() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public ShipperTaskContext ShipperTaskContext() {
        return shipperTaskContext;
    }

    //初始化部分
    @SuppressWarnings("unchecked")
    protected Input initInput(HandlerDefinition<Input> input) {
        input.getHandlerClosure().call();
        Input handler = input.getHandler();
        doInit(handler);//初始化
        if (handler.codec() == null) {
            handler.codec(shipperTaskContext.getDefaultInputCodec());//设置默认的输入编码器
            log.debug("use default input codec {}", handler.codec());
        }
        return handler;
    }

    @SuppressWarnings("unchecked")
    protected Output initOutPut(HandlerDefinition<Output> output, Map event) {
        output.getHandlerClosure().call(event);
        Output handler = output.getHandler();
        doInit(handler);
        if (handler.codec() == null) {
            handler.codec(shipperTaskContext.getDefaultOutputCodec());
            log.debug("use default output codec {}", handler.codec());
        }

        return handler;
    }

    /**
     * 任务的初始化
     */
    private void taskInit() {
        step = INITIALIZE_READY;
        initInput(shipperTaskContext.getInput());
        step = INITIALIZE_OVER;
    }

    /**
     * 对handler进行初始化操作
     *
     * @param handler handler
     */
    private void doInit(Handler handler) {
        if (handler instanceof Initialization)
            ((Initialization) handler).init();
    }

    /**
     * 对handler进行关闭操作
     *
     * @param handler handler
     */
    private void doClose(Handler handler) {
        if (handler instanceof AutoCloseable) {
            try {
                ((AutoCloseable) handler).close();
            } catch (Exception e) {
                throw new ShipperException("close handler " + handler, e);
            }
        }
    }


    /**
     * 进行读输入操作
     *
     * @return 事件
     */
    protected AbstractShipperTask doInput() {
        Input input = initInput(shipperTaskContext.getInput());
        step = WAITING_INPUT;
        log.debug("{} waiting for event", input);
        events = Collections.singletonList(input.read());
        step = INPUT_DONE;
        return this;
    }

    /**
     * 事件的过滤方式如下
     * 1. 单一事件将通过第一个filter,或许能够获取到多个事件
     * 2. 多个事件将继续输入到下一个filter
     * 3. 持续,直到没有下一个filter
     * 在执行doFilter时必须存在仅一个event,否则就会抛出一个 ShipperException
     *
     * @return shipper task
     * @throws ShipperException shipper
     */
    protected AbstractShipperTask doFilter() {
        step = WAITING_FILTER;
        DSLDelegate<Filter> filterDelegate = shipperTaskContext.getFilterDelegate();
        if (filterDelegate == null)
            return this;
        log.debug("do filter layer");
        if (events == null || events.size() > 1)
            throw new ShipperException("must exist anyone event in filter init");
        filterDelegate.getClosure().call(events.get(0));
        for (HandlerDefinition<Filter> handlerDefinition : filterDelegate.getHandlerDefinitions().values()) {
            Filter handler = handlerDefinition.getHandler();
            doInit(handler);//初始化
            List<Map> newListEvents = new ArrayList<>();
            events.forEach(aEvent -> {
                handlerDefinition.getHandlerClosure().call(aEvent);
                log.debug("do filter {}", handler);
                List<Map> newEvents = handler.filter(aEvent);
                newListEvents.addAll(newEvents);
            });
            events = newListEvents;
        }
        step = FILTER_DONE;
        return this;
    }

    /**
     * 将进行输出
     * 如果输出的handler是Recyclable而现在无法recyclable 则抛出died的异常
     *
     * @throw ShipperException output died 异常
     */
    protected void doOutPut() {
        step = WAITING_OUTPUT;
        DSLDelegate<Output> outputDelegate = shipperTaskContext.getOutputDelegate();
        events.forEach(event -> {
            log.debug("do output layer");
            outputDelegate.getClosure().call(event);
            outputDelegate.getHandlerDefinitions().forEach((k, v) -> {
                Output output = initOutPut(v, event);
                log.debug("do output {}", output);
                if (output instanceof Recyclable && !((Recyclable) output).recyclable())
                    throw new ShipperException(output + " died");
                output.write(event);
            });
        });
        step = WAITING_DONE;
    }

    protected abstract void doSomething() throws InterruptedException;


    @Override
    public ShipperTask call() throws Exception {
        try {
            taskInit();
            doSomething();
        } catch (Exception e) {
            exception(e);
        } finally {
            close();
        }
        if (!exceptions.isEmpty())
            throw new MultipleException(exceptions);
        return this;
    }

    @Override
    public void close() {
        try {
            doClose(shipperTaskContext.getInput().getHandler());
        } catch (Exception e) {
            exception(e);
        }
        for (HandlerDefinition<Output> handlerDefinition : shipperTaskContext.getOutputDelegate().getHandlerDefinitions().values()) {//将尽可能的多关闭资源,以免发生比较严重的资源泄露
            try {
                doClose(handlerDefinition.getHandler());
            } catch (Exception e) {
                exception(e);
            }
        }
        step = TASK_DONE;
    }

    protected void exception(Exception e) {
        exceptions.add(e);
    }

    @Override
    public void log(Logger log) {
        this.log = log;
    }
}
