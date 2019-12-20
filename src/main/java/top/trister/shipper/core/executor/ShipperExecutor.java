package top.trister.shipper.core.executor;

import top.trister.shipper.core.task.ShipperTaskFuture;

import java.util.List;

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
    List<ShipperTaskFuture> submit(String dsl);
}
