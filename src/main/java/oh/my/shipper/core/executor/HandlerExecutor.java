package oh.my.shipper.core.executor;

import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.enums.HandlerEnums;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface HandlerExecutor extends AutoCloseable {
    void execute(Map<HandlerEnums, DSLDelegate> dsls);
    List<CompletableFuture<List<Map>>> submit(Map<HandlerEnums, DSLDelegate> dsls);
}
