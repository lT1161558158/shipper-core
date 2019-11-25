package oh.my.shipper.core.executor;

import oh.my.shipper.core.task.ShipperTaskFuture;

import java.util.List;

public interface ShipperExecutor extends AutoCloseable {
    void execute(String dsl);
    List<ShipperTaskFuture> submit(String dsl);
}
