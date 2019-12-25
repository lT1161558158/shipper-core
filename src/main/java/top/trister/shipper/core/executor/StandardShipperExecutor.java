package top.trister.shipper.core.executor;


import lombok.Data;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import top.trister.shipper.core.BootStrapShipper;
import top.trister.shipper.core.builder.*;
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
     * 线程池
     */
    @Delegate
    private ExecutorService executorService;

    /**
     * 对shipper的 建造器
     */
    private ShipperBuilder shipperBuilder;

    public StandardShipperExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }


    @Override
    public CompletableFuture<ShipperTask> submit(ShipperTask shipperTask) {
        return CompletableFuture.supplyAsync(shipperTask::doing, executorService);
    }


    public static void main(String[] args) throws Exception {
        Stream<String> lines = new BufferedReader(new FileReader("C:\\work\\code\\java\\shipper\\src\\main\\resources\\test.shipper")).lines();
        String dsl = lines.collect(Collectors.joining("\n"));
        List<CompletableFuture<ShipperTask>> submit = BootStrapShipper.builder().build().submit(dsl);
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(submit.toArray(new CompletableFuture[0]));
        voidCompletableFuture.whenComplete((v, e) -> voidCompletableFuture.completeExceptionally(e));
        try {
            voidCompletableFuture.join();
        } catch (RuntimeException e) {
            log.error("", e);
        }
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }


}
