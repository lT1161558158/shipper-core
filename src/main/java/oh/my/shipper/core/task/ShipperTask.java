package oh.my.shipper.core.task;

public interface ShipperTask extends Runnable {
    void taskDefinition(TaskDefinition taskDefinition);
}
