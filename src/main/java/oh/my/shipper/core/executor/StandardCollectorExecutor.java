package oh.my.shipper.core.executor;


import groovy.lang.GroovyShell;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.builder.HandlerBuilder;
import oh.my.shipper.core.dsl.BaseCollectorScript;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Data
public class StandardCollectorExecutor implements CollectorExecutor {
    public static final String DEFAULT_BASE_SCRIPT = BaseCollectorScript.class.getName();
    public static final String HANDLER_BUILDER_NAME = "handlerBuilder";
    /**
     * dsl 的基础定义类
     */
    private String baseScript = DEFAULT_BASE_SCRIPT;
    private HandlerBuilder handlerBuilder;

    public StandardCollectorExecutor(HandlerBuilder handlerBuilder) {
        this.handlerBuilder = handlerBuilder;
    }

    @Override
    public void executor(String dsl) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(baseScript);
        GroovyShell groovyShell = new GroovyShell(compilerConfiguration);
        groovyShell.setVariable(HANDLER_BUILDER_NAME, handlerBuilder);
        groovyShell.evaluate(dsl);
    }

    public static void main(String[] args) throws IOException {
        Stream<String> lines = new BufferedReader(new FileReader("C:\\work\\code\\java\\shipper\\src\\main\\resources\\test.shipper")).lines();
        String dsl = lines.collect(Collectors.joining("\n"));
        HandlerBuilder handlerBuilder = new HandlerBuilder();
        handlerBuilder.reLoadHandler();
        StandardCollectorExecutor standardCollectorExecutor = new StandardCollectorExecutor(handlerBuilder);
        try{
            standardCollectorExecutor.executor(dsl);
        }catch (RuntimeException e){
            log.error("dsl error [{}]",e.getMessage());
        }
    }
}
