package top.trister.shipper.core.executor;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.trister.shipper.core.builder.*;
import top.trister.shipper.core.exception.MultipleException;
import top.trister.shipper.core.task.ShipperTask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.*;
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

    public StandardShipperExecutor(ShipperBuilder shipperBuilder, ShipperTaskBuilder shipperTaskBuilder, ExecutorService executorService) {
        this.shipperTaskBuilder = shipperTaskBuilder;
        this.executorService = executorService;
        this.shipperBuilder = shipperBuilder;
    }

    @Override
    public void execute(String shipper) {
        submit(shipper);
    }

    @Override
    public List<CompletableFuture<ShipperTask>> submit(String shipper) {
        return shipperTaskBuilder
                .build(shipperBuilder.build(shipper))
                .stream()
                .map(t -> CompletableFuture.supplyAsync(t::doing, executorService))
                .collect(Collectors.toList());
    }


    public static void main(String[] args) throws Exception {
        Stream<String> lines = new BufferedReader(new FileReader("C:\\work\\code\\java\\shipper\\src\\main\\resources\\test.shipper")).lines();
        String dsl = lines.collect(Collectors.joining("\n"));
        HandlerBuilder standardHandlerBuilder = new StandardHandlerBuilder();
        standardHandlerBuilder.reLoadHandler();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler((t, e) -> log.error("shipper executor error", e));
            return thread;
        });
        ShipperBuilder shipperBuilder = new StandardShipperBuilder(standardHandlerBuilder);
        ShipperTaskBuilder shipperTaskBuilder = new StandardShipperTaskBuilder();
        try (ShipperExecutor standardShipperExecutor = new StandardShipperExecutor(shipperBuilder, shipperTaskBuilder, threadPoolExecutor)) {
            List<CompletableFuture<ShipperTask>> submit = standardShipperExecutor.submit(dsl);
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(submit.toArray(new CompletableFuture[0]));
            voidCompletableFuture.whenComplete((v,e)->{
                voidCompletableFuture.completeExceptionally(e);
            });
            try{
                voidCompletableFuture.join();
            }catch (Exception e){
                Throwable cause = e.getCause();
                if (cause instanceof MultipleException)
                    System.out.println(((MultipleException) cause).getExceptions());
            }

        }
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }


}
