package top.trister.shipper.core.builder;

import groovy.lang.GroovyShell;
import top.trister.shipper.core.bean.Shipper;
import top.trister.shipper.core.dsl.BaseShipperScript;
import top.trister.shipper.core.dsl.DSLDelegate;
import top.trister.shipper.core.enums.HandlerEnums;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.HashMap;
import java.util.Map;

public class StandardShipperBuilder implements ShipperBuilder {
    public static final String DEFAULT_BASE_SCRIPT = BaseShipperScript.class.getName();
    public static final String HANDLER_BUILDER_NAME = "handlerBuilder";
    public static final String HANDLER_EXECUTOR_NAME = "shipperTaskBuilder";
    public static final String HANDLER_MAP_NAME = "handlerMap";
    public static final String COMPLETABLE_FUTURE_NAME = "completableFuture";
    /**
     * dsl 的基础定义类名
     */
    private String baseScript = DEFAULT_BASE_SCRIPT;
    /**
     * 处理器build工厂
     */
    private HandlerBuilder standardHandlerBuilder;

    public StandardShipperBuilder(HandlerBuilder standardHandlerBuilder) {
        this.standardHandlerBuilder = standardHandlerBuilder;
    }

    @Override
    public Shipper build(String shipper) {
        Map<HandlerEnums, DSLDelegate> handlerMap = new HashMap<>();
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(baseScript);
        GroovyShell groovyShell = new GroovyShell(compilerConfiguration);
        groovyShell.setVariable(HANDLER_BUILDER_NAME, standardHandlerBuilder);
        groovyShell.setVariable(HANDLER_MAP_NAME, handlerMap);
        groovyShell.evaluate(shipper);
        Shipper result = new Shipper();
        result.setContext(handlerMap);
        result.setShipperDescribe(shipper);
        return result;
    }
}
