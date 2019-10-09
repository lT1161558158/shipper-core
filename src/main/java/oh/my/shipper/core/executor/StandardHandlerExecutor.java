package oh.my.shipper.core.executor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.api.Filter;
import oh.my.shipper.core.api.Handler;
import oh.my.shipper.core.api.Input;
import oh.my.shipper.core.api.Output;
import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.dsl.HandlerDefinition;
import oh.my.shipper.core.enums.HandlerEnums;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Data
public class StandardHandlerExecutor implements HandlerExecutor {

    private ExecutorService executorService;

    public StandardHandlerExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void execute(Map<HandlerEnums, DSLDelegate> dsls) {
        DSLDelegate inputDelegate = dsls.get(HandlerEnums.INPUT);
        DSLDelegate outputDelegate = dsls.get(HandlerEnums.OUTPUT);
        DSLDelegate filterDelegate = dsls.get(HandlerEnums.FILTER);
        if (outputDelegate == null) {
            log.error("at least one output must be provided");
            return;
        }
        if (inputDelegate == null) {
            log.error("at least one input must be provided");
            return;
        }
        inputDelegate.getClosure().call();//创建input的上下文
        List<HandlerDefinition> handlerDefinitions = inputDelegate.getHandlerDefinitions();
        handlerDefinitions.stream().map(handlerDefinition -> buildFuture(handlerDefinition, filterDelegate, outputDelegate)).forEach(CompletableFuture::join);
    }

    private CompletableFuture buildFuture(HandlerDefinition input, DSLDelegate filterDelegate, DSLDelegate outputDelegate) {
        return CompletableFuture.supplyAsync(() -> {
            input.getHandlerClosure().call();
            Handler inputHandler = input.getHandler();
            if (!(inputHandler instanceof Input))
                return null;   
            return ((Input) inputHandler).read();
        }, executorService).thenApply(event -> {
            List<Map> events = new ArrayList<>();
            events.add(event);
            if (filterDelegate == null)
                return events;
            filterDelegate.getClosure().call(event);
            for (HandlerDefinition handlerDefinition : filterDelegate.getHandlerDefinitions()) {
                Handler handler = handlerDefinition.getHandler();
                if (handler instanceof Filter) {
                    List<Map> newListEvents = new ArrayList<>();
                    events.forEach(aEvent -> {
                        handlerDefinition.getHandlerClosure().call(aEvent);
                        List<Map> newEvents = ((Filter) handler).filter(aEvent);
                        newListEvents.addAll(newEvents);
                    });
                    events = newListEvents;
                }
            }
            return events;
        }).thenAccept(events -> {
            outputDelegate.getClosure().call();
            for (HandlerDefinition handlerDefinition : outputDelegate.getHandlerDefinitions()) {
                Handler handler = handlerDefinition.getHandler();
                if (handler instanceof Output) {
                    events.forEach(event -> {
                        handlerDefinition.getHandlerClosure().call(event);
                        ((Output) handler).write(event);
                    });
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}
