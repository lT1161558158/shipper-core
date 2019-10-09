package oh.my.shipper.core.executor;


import groovy.lang.GroovyShell;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.builder.HandlerBuilder;
import oh.my.shipper.core.builder.ShipperTaskBuilder;
import oh.my.shipper.core.builder.StandardShipperTaskBuilder;
import oh.my.shipper.core.dsl.BaseShipperScript;
import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.enums.HandlerEnums;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Stream<String> lines = new BufferedReader(new FileReader("C:\\work\\code\\java\\shipper\\src\\main\\resources\\test.shipper")).lines();
        String dsl = lines.collect(Collectors.joining("\n"));
        HandlerBuilder handlerBuilder = new HandlerBuilder();
        handlerBuilder.reLoadHandler();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setUncaughtExceptionHandler((t, e) -> log.error("shipper executor error", e));
                return thread;
            }
        });
        ShipperTaskBuilder ShipperTaskBuilder = new StandardShipperTaskBuilder();
        try (ShipperExecutor standardShipperExecutor = new StandardShipperExecutor(handlerBuilder, ShipperTaskBuilder,threadPoolExecutor)) {
            standardShipperExecutor.execute(dsl);
        } catch (RuntimeException e) {
            log.error("dsl error [{}]", e.getMessage());
        }

    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}
