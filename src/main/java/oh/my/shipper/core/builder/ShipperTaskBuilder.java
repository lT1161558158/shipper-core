package oh.my.shipper.core.builder;

import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.enums.HandlerEnums;

import java.util.List;
import java.util.Map;

public interface ShipperTaskBuilder {
    List<Runnable> builderTask(Map<HandlerEnums, DSLDelegate> dsls);
}
