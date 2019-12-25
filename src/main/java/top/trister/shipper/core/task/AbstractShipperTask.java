package top.trister.shipper.core.task;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.trister.shipper.core.api.Initialization;
import top.trister.shipper.core.api.handler.CodifiedHandler;
import top.trister.shipper.core.api.handler.Handler;
import top.trister.shipper.core.api.handler.input.CodecInput;
import top.trister.shipper.core.api.handler.input.Input;
import top.trister.shipper.core.api.handler.mapping.Mapping;
import top.trister.shipper.core.api.handler.output.CodecOutput;
import top.trister.shipper.core.api.handler.output.Output;
import top.trister.shipper.core.dsl.DSLDelegate;
import top.trister.shipper.core.dsl.HandlerDefinition;
import top.trister.shipper.core.exception.MultipleException;
import top.trister.shipper.core.exception.ShipperException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static top.trister.shipper.core.task.TaskStepEnum.*;

@Data
public abstract class AbstractShipperTask implements ShipperTask, AutoCloseable, LogAware {

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
     * 封闭在线程内部的events对象引用
     */
    protected final AtomicReference<Object> eventRef = new AtomicReference<>();

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
    public Object nowEvents() {
        return eventRef.get();
    }

    @Override
    public ShipperTaskContext ShipperTaskContext() {
        return shipperTaskContext;
    }

    //初始化部分
    @SuppressWarnings("unchecked")
    protected Input initInput(HandlerDefinition<Input> input) {
        return doInit(input);//初始化
    }

    protected Output initOutPut(HandlerDefinition<Output> output) {
        return doInit(output);
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
     * @param handlerDefinition 处理器描述
     * @return handler
     */
    @SuppressWarnings("unchecked")
    protected <T extends Handler> T doInit(HandlerDefinition<T> handlerDefinition) {
        //执行 groovy
        T handler = handlerDefinition.getHandler();
        if (handler instanceof Input)
            handlerDefinition.getHandlerClosure().call();//执行输入时,没有事件输入
        else
            handlerDefinition.getHandlerClosure().call(eventRef.get());//其他情况有事件输入
        //执行初始化函数
        if (handler instanceof Initialization)
            ((Initialization) handler).init();
        //设置默认的编解码器
        if (handler instanceof CodifiedHandler) {
            CodifiedHandler codifiedHandler = (CodifiedHandler) handler;
            if (codifiedHandler.codec() == null) {
                if (handler instanceof Input) {
                    codifiedHandler.codec(shipperTaskContext.getDefaultInputCodec());//设置默认的输入编码器
                    log.debug("use default input codec {}", codifiedHandler.codec());
                } else if (handler instanceof Output) {
                    codifiedHandler.codec(shipperTaskContext.getDefaultOutputCodec());//设置默认的输出编码器
                    log.debug("use default output codec {}", codifiedHandler.codec());
                } else {
                    log.warn("Mapping's codec cannot be provided by default");
                }
            }
        }
        return handler;
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
        eventRef.set(input instanceof CodecInput ? ((CodecInput) input).codecRead() : input.read());
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
    @SuppressWarnings("unchecked")
    protected AbstractShipperTask doFilter() {
        step = WAITING_FILTER;
        DSLDelegate<Mapping> filterDelegate = shipperTaskContext.getFilterDelegate();
        if (filterDelegate == null)
            return this;
        log.debug("do filter layer");
        filterDelegate.getClosure().call(eventRef.get());//初始化mapping层
        filterDelegate.getHandlerDefinitions()
                .values().stream()
                .map(this::doInit)
                .forEach(h -> {
                    log.debug("do filter {}", h);
                    eventRef.set(h.mapping(eventRef.get()));
                });
        step = FILTER_DONE;
        return this;
    }

    /**
     * 将进行输出
     * 如果输出的handler是Recyclable而现在无法recyclable 则抛出died的异常
     *
     * @throw ShipperException output died 异常
     */
    @SuppressWarnings("unchecked")
    protected void doOutPut() {
        step = WAITING_OUTPUT;
        DSLDelegate<Output> outputDelegate = shipperTaskContext.getOutputDelegate();
        log.debug("do output layer");
        outputDelegate.getClosure().call(eventRef.get());//初始化output层
        outputDelegate.getHandlerDefinitions()
                .values().stream()
                .map(this::doInit)
                .forEach(h -> {
                    log.debug("do output {}", h);
                    if (h instanceof CodecOutput)
                        ((CodecOutput) h).codecWrite(eventRef.get());
                    else
                        h.write(eventRef.get());
                });
        step = WAITING_DONE;
    }

    protected abstract void doSomething() throws InterruptedException;


    @Override
    public void run() {
        try {
            taskInit();
            doSomething();
        } catch (Exception e) {
            exception(e);
            throw new ShipperException(e);
        } finally {
            close();
        }
        tryThrowException();
    }

    /**
     * 尝试抛出运行时的异常
     */
    private void tryThrowException() {
        if (!exceptions.isEmpty()) {
            String multipleException = exceptions.stream().map(Exception::getMessage).collect(Collectors.joining(";"));
            throw new MultipleException(multipleException, exceptions);
        }
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
