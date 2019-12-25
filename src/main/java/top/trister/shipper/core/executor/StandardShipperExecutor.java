package top.trister.shipper.core.executor;


import lombok.Data;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import top.trister.shipper.core.BootStrapShipper;
import top.trister.shipper.core.builder.ShipperBuilder;
import top.trister.shipper.core.task.ShipperTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
        BootStrapShipper.builder().build().executeBySource("test.shipper");
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }


}
