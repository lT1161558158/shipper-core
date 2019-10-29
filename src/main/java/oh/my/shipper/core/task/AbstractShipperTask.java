package oh.my.shipper.core.task;

import lombok.Data;
import oh.my.shipper.core.api.*;
import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.dsl.HandlerDefinition;
import oh.my.shipper.core.exception.ShipperException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public abstract class AbstractShipperTask implements ShipperTask, AutoCloseable {
    protected String name;
    protected TaskDefinition taskDefinition;

    @Override
    public String name() {
        return name;
    }

    /**
     * @param handler 对handler进行初始化操作
     */
    private void doInit(Handler handler) {
        if (handler instanceof Initialization)
            ((Initialization) handler).init();
    }

    @SuppressWarnings("unchecked")
    protected Input initInput(HandlerDefinition<Input> input) {
        input.getHandlerClosure().call();
        Input handler = input.getHandler();
        doInit(handler);//初始化
        if (handler.codec() == null)
            handler.codec(taskDefinition.getDefaultInputCodec());//设置默认的输入编码器
        return handler;
    }
    @SuppressWarnings("unchecked")
    protected Output initOutPut(HandlerDefinition<Output> output,Map event){
        output.getHandlerClosure().call(event);
        Output handler = output.getHandler();
        doInit(handler);
        if (handler.codec() == null)
            handler.codec(taskDefinition.getDefaultOutputCodec());
        return handler;
    }

    /**
     * 事件的过滤方式如下
     * 1. 单一事件将通过第一个filter,或许能够获取到多个事件
     * 2. 多个事件将继续输入到下一个filter
     * 3. 持续,直到没有下一个filter
     *
     * @param filterDelegate 过滤层
     * @param event          事件
     * @return 一系列事件
     */
    protected List<Map> doFilter(DSLDelegate<Filter> filterDelegate, Map event) {
        List<Map> events = new ArrayList<>();
        events.add(event);
        if (filterDelegate == null)
            return events;
        filterDelegate.getClosure().call(event);
        for (HandlerDefinition<Filter> handlerDefinition : filterDelegate.getHandlerDefinitions()) {
            Filter handler = handlerDefinition.getHandler();
            doInit(handler);//初始化
            List<Map> newListEvents = new ArrayList<>();
            events.forEach(aEvent -> {
                handlerDefinition.getHandlerClosure().call(aEvent);
                List<Map> newEvents = handler.filter(aEvent);
                newListEvents.addAll(newEvents);
            });
            events = newListEvents;
        }
        return events;
    }

    /**
     * 将进行输出
     *
     * @param outputDelegate 输出层的代理
     * @param events         等待输出的事件
     */
    protected void doOutPut(DSLDelegate<Output> outputDelegate, List<Map> events) {
        AtomicInteger counter=new AtomicInteger(0);
        events.forEach(event -> {
            outputDelegate.getClosure().call(event);
            outputDelegate.getHandlerDefinitions().forEach(handlerDefinition -> {
                Output output = initOutPut(handlerDefinition, event);
                if (output instanceof Recyclable && !((Recyclable) output).recyclable())
                    throw new ShipperException(output+" died");
                output.write(event);
                counter.incrementAndGet();
            });
        });
    }

    protected abstract void doSomething() throws InterruptedException;

    @Override
    public void run() {
        try {
            initInput(taskDefinition.getInput());
            doSomething();
        } catch (Exception e) {
            throw new ShipperException(e);
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        try {
            Input handler = taskDefinition.getInput().getHandler();
            if (handler instanceof AutoCloseable)
                ((AutoCloseable) handler).close();
        } catch (Exception e) {
            throw new ShipperException("close input ", e);
        }
        Exception e = null;
        for (HandlerDefinition<Output> handlerDefinition : taskDefinition.getOutputDelegate().getHandlerDefinitions()) {//将尽可能的多关闭资源,以免发生比较严重的资源泄露
            Output handler = handlerDefinition.getHandler();
            if (handler instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) handler).close();
                } catch (Exception ex) {
                    e = ex;
                }
            }
        }
        if (e != null)
            throw new ShipperException("close output ", e);
    }
}
