package oh.my.shipper.core.task;

/**
 * 循环的ShipperTask的实现
 */
public interface LoopShipperTask extends ShipperTask {
    boolean loop();
}
