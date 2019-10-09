package oh.my.shipper.core.executor;

public interface ShipperExecutor extends AutoCloseable {
    void execute(String dsl);
}
