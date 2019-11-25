package oh.my.shipper.core.executor;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.builder.*;
import oh.my.shipper.core.task.ShipperTaskFuture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Data
public class StandardShipperExecutor implements ShipperExecutor {



    /**
     * shipper task 的建造器
     */
    private ShipperTaskBuilder shipperTaskBuilder;
    /**
     * 线程池
     */
    private ExecutorService executorService;

    /**
     * 对shipper的 建造器
     */
    private ShipperBuilder shipperBuilder;

    public StandardShipperExecutor(ShipperBuilder shipperBuilder,ShipperTaskBuilder shipperTaskBuilder, ExecutorService executorService) {
        this.shipperTaskBuilder = shipperTaskBuilder;
        this.executorService = executorService;
        this.shipperBuilder = shipperBuilder;
    }

    @Override
    public void execute(String dsl) {
        submit(dsl);
    }

    @Override
    public List<ShipperTaskFuture> submit(String dsl) {
        return shipperTaskBuilder
                .build(shipperBuilder.build(dsl))
                .stream()
                .map(task->new ShipperTaskFuture<>(task,executorService.submit(task)))
                .collect(Collectors.toList());
    }



    public static void main(String[] args) throws Exception {
        Stream<String> lines = new BufferedReader(new FileReader("C:\\work\\code\\java\\shipper\\src\\main\\resources\\test.shipper")).lines();
        String dsl = lines.collect(Collectors.joining("\n"));
        StandardHandlerBuilder standardHandlerBuilder = new StandardHandlerBuilder();
        standardHandlerBuilder.reLoadHandler();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler((t, e) -> log.error("shipper executor error", e));
            return thread;
        });
        ShipperBuilder shipperBuilder=new StandardShipperBuilder(standardHandlerBuilder);
        ShipperTaskBuilder shipperTaskBuilder = new StandardShipperTaskBuilder();
        try (ShipperExecutor standardShipperExecutor = new StandardShipperExecutor(shipperBuilder, shipperTaskBuilder, threadPoolExecutor)) {
            standardShipperExecutor.execute(dsl);
        }
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}
