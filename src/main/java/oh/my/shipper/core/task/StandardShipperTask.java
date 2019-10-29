package oh.my.shipper.core.task;

import lombok.Builder;
import lombok.Data;
import oh.my.shipper.core.api.*;
import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.dsl.HandlerDefinition;
import oh.my.shipper.core.exception.ShipperException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class StandardShipperTask implements ShipperTask, AutoCloseable {
    private String name;
    private HandlerDefinition<Input> input;
    private DSLDelegate<Filter> filterDelegate;
    private DSLDelegate<Output> outputDelegate;
    private InputCodec<?> defaultInputCodec;
    private OutCodec<?> defaultOutputCodec;

    private boolean loop;

    @Override
    public String name() {
        return name;
    }

    private void doInit(Handler handler) {
        if (handler instanceof Initialization)
            ((Initialization) handler).init();
    }

    /**
     * @param input 一个输入的处理器描述
     * @return 一个事件
     */
    @SuppressWarnings("unchecked")
    private Map readInput(HandlerDefinition<Input> input) {
        input.getHandlerClosure().call();
        Input handler = input.getHandler();
        doInit(handler);//初始化
        if (handler.codec() == null)
            handler.codec(defaultInputCodec);//设置默认的输入编码器
        if (handler instanceof Recyclable)
            return (loop = ((Recyclable) handler).recyclable()) ? handler.read() : null;
        else
            return handler.read();
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
    private List<Map> doFilter(DSLDelegate<Filter> filterDelegate, Map event) {
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
    @SuppressWarnings("unchecked")
    private void doOutPut(DSLDelegate<Output> outputDelegate, List<Map> events) {
        events.forEach(event -> {
            outputDelegate.getClosure().call(event);
            outputDelegate.getHandlerDefinitions().forEach(handlerDefinition -> {
                Output handler = handlerDefinition.getHandler();
                doInit(handler);
                handlerDefinition.getHandlerClosure().call(event);
                if (handler.codec() == null)
                    handler.codec(defaultOutputCodec);
                handler.write(event);
            });
        });
    }

    @Override
    public void run() {
        Map event;
        try {
            while (!Thread.currentThread().isInterrupted() && (event = readInput(input)) != null) {
                List<Map> events = doFilter(filterDelegate, event);
                doOutPut(outputDelegate, events);
                if (!loop)//不循环的情况下一次就会退出,可循环的情况下,若还可循环,则继续
                    break;
            }
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        try {
            Input handler = input.getHandler();
            if (handler instanceof AutoCloseable)
                ((AutoCloseable) handler).close();
        } catch (Exception e) {
            throw new ShipperException("close input ", e);
        }
        Exception e = null;
        for (HandlerDefinition<Output> handlerDefinition : outputDelegate.getHandlerDefinitions()) {//将尽可能的多关闭资源,以免发生比较严重的资源泄露
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
