package oh.my.shipper.core.executor;

import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.builder.HandlerBuilder;
import oh.my.shipper.core.builder.ShipperTaskBuilder;
import oh.my.shipper.core.builder.StandardShipperTaskBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Slf4j
public class StandardShipperExecutorTest {
    @Test
    public void test() throws Exception {
        Stream<String> lines = new BufferedReader(new FileReader("C:\\work\\code\\java\\shipper\\src\\main\\resources\\test.shipper")).lines();
        String dsl = lines.collect(Collectors.joining("\n"));
        HandlerBuilder handlerBuilder = new HandlerBuilder();
        handlerBuilder.reLoadHandler();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler((t, e) -> log.error("shipper executor error", e));
            return thread;
        });
        ShipperTaskBuilder ShipperTaskBuilder = new StandardShipperTaskBuilder();
        try (ShipperExecutor standardShipperExecutor = new StandardShipperExecutor(handlerBuilder, ShipperTaskBuilder, threadPoolExecutor)) {
            standardShipperExecutor.execute(dsl);
        } catch (RuntimeException e) {
            log.error("dsl error [{}]", e.getMessage());
        }

    }
}