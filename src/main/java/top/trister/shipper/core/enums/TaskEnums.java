package top.trister.shipper.core.enums;

import top.trister.shipper.core.api.handler.input.Input;
import top.trister.shipper.core.api.Recyclable;
import top.trister.shipper.core.api.Scheduled;
import top.trister.shipper.core.task.ShipperTask;
import top.trister.shipper.core.task.StandardLoopShipperTask;
import top.trister.shipper.core.task.StandardScheduleShipperTask;
import top.trister.shipper.core.task.StandardSimpleShipperTask;

/**
 * 通过映射接口和builder,然后使用反射构造相应的task
 */
public enum TaskEnums {
    SIMPLE(Input.class, "simple", StandardSimpleShipperTask.class),
    LOOP(Recyclable.class, "loop", StandardLoopShipperTask.class),
    CRON(Scheduled.class, "cron", StandardScheduleShipperTask.class);
    Class<?> typeClazz;
    String type;
    Class<? extends ShipperTask> taskImplClazz;

    TaskEnums(Class<?> typeClazz, String type, Class<? extends ShipperTask> taskImplClazz) {
        this.typeClazz = typeClazz;
        this.type = type;
        this.taskImplClazz = taskImplClazz;
    }

    public static TaskEnums valueOf(Class task) {
        for (TaskEnums value : values()) {
            if (value.typeClazz.isAssignableFrom(task)) {
                return value;
            }
        }
        return null;
    }

}
