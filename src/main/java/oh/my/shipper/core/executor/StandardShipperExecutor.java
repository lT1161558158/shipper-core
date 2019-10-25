package oh.my.shipper.core.executor;


import groovy.lang.GroovyShell;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.builder.HandlerBuilder;
import oh.my.shipper.core.builder.ShipperTaskBuilder;
import oh.my.shipper.core.dsl.BaseShipperScript;
import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.enums.HandlerEnums;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Slf4j
@Data
public class StandardShipperExecutor implements ShipperExecutor {
    public static final String DEFAULT_BASE_SCRIPT = BaseShipperScript.class.getName();
    public static final String HANDLER_BUILDER_NAME = "handlerBuilder";
    public static final String HANDLER_EXECUTOR_NAME = "shipperTaskBuilder";
    public static final String HANDLER_MAP_NAME = "handlerMap";

    public static final String COMPLETABLE_FUTURE_NAME = "completableFuture";

    /**
     * dsl 的基础定义类
     */
    private String baseScript = DEFAULT_BASE_SCRIPT;
    private HandlerBuilder handlerBuilder;
    private ShipperTaskBuilder shipperTaskBuilder;
    private ExecutorService executorService;


    public StandardShipperExecutor(HandlerBuilder handlerBuilder, ShipperTaskBuilder shipperTaskBuilder, ExecutorService executorService) {
        this.handlerBuilder = handlerBuilder;
        this.shipperTaskBuilder = shipperTaskBuilder;
        this.executorService = executorService;
    }

    @Override
    public void execute(String dsl) {
        shipperTaskBuilder.builderTask(buildHandlerMap(dsl)).forEach(executorService::execute);
    }


    private Map<HandlerEnums, DSLDelegate> buildHandlerMap(String dsl) {
        Map<HandlerEnums, DSLDelegate> handlerMap = new HashMap<>();
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(baseScript);
        GroovyShell groovyShell = new GroovyShell(compilerConfiguration);
        groovyShell.setVariable(HANDLER_BUILDER_NAME, handlerBuilder);
        groovyShell.setVariable(HANDLER_EXECUTOR_NAME, shipperTaskBuilder);
        groovyShell.setVariable(HANDLER_MAP_NAME, handlerMap);
        groovyShell.evaluate(dsl);
        return handlerMap;
    }

    public static void main(String[] args) throws Exception {

    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}
