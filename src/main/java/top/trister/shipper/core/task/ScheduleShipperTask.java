package top.trister.shipper.core.task;

import top.trister.shipper.core.api.Scheduled;

/**
 * 可调度的 ShipperTask
 */
public interface ScheduleShipperTask extends ShipperTask, Scheduled { }
