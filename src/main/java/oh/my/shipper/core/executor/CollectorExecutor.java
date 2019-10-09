package oh.my.shipper.core.executor;

public interface CollectorExecutor extends AutoCloseable {
    void executor(String dsl);
}
