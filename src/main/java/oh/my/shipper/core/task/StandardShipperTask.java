package oh.my.shipper.core.task;

import lombok.Builder;
import lombok.Data;
import oh.my.shipper.core.api.Filter;
import oh.my.shipper.core.api.Input;
import oh.my.shipper.core.api.Output;
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

    @Override
    public String name() {
        return name;
    }

    private Map readInput(HandlerDefinition<Input> input) {
        input.getHandlerClosure().call();
        Input inputHandler = input.getHandler();
        return inputHandler.ready() ? inputHandler.read() : null;
    }

    private List<Map> doFilter(DSLDelegate<Filter> filterDelegate, Map event) {
        List<Map> events = new ArrayList<>();
        events.add(event);
        if (filterDelegate == null)
            return events;
        filterDelegate.getClosure().call(event);

        for (HandlerDefinition<Filter> handlerDefinition : filterDelegate.getHandlerDefinitions()) {
            Filter handler = handlerDefinition.getHandler();
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

    private void doOutPut(DSLDelegate<Output> outputDelegate, List<Map> events) {
        events.forEach(event -> {
            outputDelegate.getClosure().call(event);
            outputDelegate.getHandlerDefinitions().forEach(handlerDefinition -> {
                Output handler = handlerDefinition.getHandler();
                handlerDefinition.getHandlerClosure().call(event);
                if (handler.writeAble())
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
            }
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        try {
            input.getHandler().close();
        } catch (Exception e) {
            throw new ShipperException("close input ", e);
        }
        Exception e = null;
        for (HandlerDefinition<Output> handlerDefinition : outputDelegate.getHandlerDefinitions()) {
            try {
                handlerDefinition.getHandler().close();
            } catch (Exception ex) {
                e = ex;
            }
        }
        if (e != null)
            throw new ShipperException("close output ", e);
    }
}
