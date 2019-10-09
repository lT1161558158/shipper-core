package oh.my.shipper.core.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CollectorExecutor extends AutoCloseable {
    void executor(String dsl);
    List<CompletableFuture<List<Map>>> submit(String dsl);
}
