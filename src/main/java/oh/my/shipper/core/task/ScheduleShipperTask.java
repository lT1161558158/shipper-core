package oh.my.shipper.core.task;

/**
 * 可调度的 ShipperTask
 */
public interface ScheduleShipperTask extends ShipperTask {
    boolean trigger();
}
