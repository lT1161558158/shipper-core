package top.trister.shipper.core.executor;

import top.trister.shipper.core.task.ShipperTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * shipper的执行器
 */
public interface ShipperExecutor extends AutoCloseable {
    /**
     *  just run
     * @param dsl dsl
     */
    void execute(String dsl);

    /**
     *
     * @param dsl dsl
     * @return 执行中的future
     */
    List<CompletableFuture<ShipperTask>> submit(String dsl);
}
