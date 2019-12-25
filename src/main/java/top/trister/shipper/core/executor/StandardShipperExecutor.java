package top.trister.shipper.core.executor;


import lombok.Data;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import top.trister.shipper.core.BootStrapShipper;
import top.trister.shipper.core.builder.ShipperBuilder;
import top.trister.shipper.core.task.ShipperTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

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
        Optional.ofNullable(Thread.currentThread().getContextClassLoader().getResource("test.shipper")).ifPresent(url -> {
            try {
                String dsl = new BufferedReader(new InputStreamReader(url.openStream()))
                        .lines()
                        .collect(Collectors.joining("\n"));
                List<CompletableFuture<ShipperTask>> submit = BootStrapShipper.builder().build().submit(dsl);
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(submit.toArray(new CompletableFuture[0]));
                voidCompletableFuture.whenComplete((v, e) -> voidCompletableFuture.completeExceptionally(e));
                voidCompletableFuture.join();
            } catch (IOException e) {
                log.error("", e);
            }
        });
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }


}
