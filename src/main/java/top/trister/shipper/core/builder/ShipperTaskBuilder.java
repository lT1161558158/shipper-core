package top.trister.shipper.core.builder;

import top.trister.shipper.core.bean.Shipper;
import top.trister.shipper.core.task.ShipperTask;

import java.util.List;

/**
 * 将shipper转化为ShipperTask
 */
public interface ShipperTaskBuilder {
    List<ShipperTask> build(Shipper shipper);
}
