package oh.my.shipper.core.executor;


import groovy.lang.GroovyShell;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.builder.HandlerBuilder;
import oh.my.shipper.core.dsl.BaseShipperScript;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Data
public class StandardCollectorExecutor implements CollectorExecutor {
    public static final String DEFAULT_BASE_SCRIPT = BaseShipperScript.class.getName();
    public static final String HANDLER_BUILDER_NAME = "handlerBuilder";
    public static final String HANDLER_EXECUTOR_NAME =  "handlerExecutor";
    /**
     * dsl 的基础定义类
     */
    private String baseScript = DEFAULT_BASE_SCRIPT;
    private HandlerBuilder handlerBuilder;
    private HandlerExecutor handlerExecutor;

    public StandardCollectorExecutor(HandlerBuilder handlerBuilder, HandlerExecutor handlerExecutor) {
        this.handlerBuilder = handlerBuilder;
        this.handlerExecutor = handlerExecutor;
    }

    @Override
    public void executor(String dsl) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(baseScript);
        GroovyShell groovyShell = new GroovyShell(compilerConfiguration);
        groovyShell.setVariable(HANDLER_BUILDER_NAME, handlerBuilder);
        groovyShell.setVariable(HANDLER_EXECUTOR_NAME,handlerExecutor);
        groovyShell.evaluate(dsl);
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
                thread.setUncaughtExceptionHandler((t, e) -> log.error("",e));
                return thread;
            }
        });

        HandlerExecutor handlerExecutor=new StandardHandlerExecutor(threadPoolExecutor);
        StandardCollectorExecutor standardCollectorExecutor = new StandardCollectorExecutor(handlerBuilder,handlerExecutor);
        try{
            standardCollectorExecutor.executor(dsl);
        }catch (RuntimeException e){
            log.error("dsl error [{}]",e.getMessage());
        }
        standardCollectorExecutor.close();
    }

    @Override
    public void close() throws Exception {
        handlerExecutor.close();
    }
}
