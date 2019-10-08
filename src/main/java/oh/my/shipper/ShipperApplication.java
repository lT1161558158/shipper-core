package oh.my.shipper;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@SpringBootApplication
public class ShipperApplication {
    private static final String GROK_PATTERN_PATH = "E:\\patterns\\grok-patterns";

    public static void main(String[] args) throws ScriptException {
//        SpringApplication.run(CollectorApplication.class, args);
//        String pattern="%{MONTH}\\s+%{MONTHDAY}\\s+%{TIME}\\s+%{YEAR}.*%{fromIP}";
//        String message = "Mon Nov  9 06:47:33 2015; UDP; eth1; 461 bytes; from 88.150.240.169:tag-pm";
//
//        Grok grok=new Grok();
//        grok.addPatternFromFile(GROK_PATTERN_PATH);
//        grok.addPattern("fromIP", "%{IPV4}");
//
//        grok.compile(pattern);
//        Match match = grok.match(message);
//        match.captures();
//        if(!match.isNull()){
//            System.out.println(match.toMap());
////            System.out.println(match.getSubject());
//        }else{
//            System.out.println("not match");
//        }
//        StdInput input=new StdInput();
//        Event event = input.read();
//
//        output.write(event);
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine groovy = factory.getEngineByExtension("groovy");
        groovy.eval("println 1+1");
        System.out.println(groovy);
    }
}
