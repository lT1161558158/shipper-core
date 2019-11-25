package oh.my.shipper.core.builder;

import oh.my.shipper.core.bean.Shipper;
import oh.my.shipper.core.task.ShipperTask;

import java.util.List;

public interface ShipperTaskBuilder {
    List<ShipperTask> build(Shipper shipper);
}
