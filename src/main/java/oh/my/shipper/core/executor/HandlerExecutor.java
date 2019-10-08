package oh.my.shipper.core.executor;

import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.enums.HandlerEnums;

import java.util.Map;

public interface HandlerExecutor {
    void execute(Map<HandlerEnums, DSLDelegate> dsls);
}
