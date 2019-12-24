package top.trister.shipper.core.task;

public interface ShipperTaskContextAware {
    /**
     *
     * @param shipperTaskContext 任务的描述信息
     */
    void shipperTaskContext(ShipperTaskContext shipperTaskContext);

    /**
     *
     * @return shipperTaskContext 任务的描述信息
     */
    ShipperTaskContext ShipperTaskContext();
}
