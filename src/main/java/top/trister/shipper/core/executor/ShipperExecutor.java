package top.trister.shipper.core.executor;

import top.trister.shipper.core.task.ShipperTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * shipper的执行器
 */
public interface ShipperExecutor extends AutoCloseable, ExecutorService {
    /**
     * just run
     *
     * @param shipperTask shipperTask
     */
    default void execute(ShipperTask shipperTask) {
        submit(shipperTask);
    }


    /**
     * @param shipperTask shipperTask
     * @return 执行中的future
     */
    CompletableFuture<ShipperTask> submit(ShipperTask shipperTask);

}
