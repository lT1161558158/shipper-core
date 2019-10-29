package oh.my.shipper.core.task;

public interface ScheduleShipperTask extends ShipperTask {
    boolean trigger();
}
