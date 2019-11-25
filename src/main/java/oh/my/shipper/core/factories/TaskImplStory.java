package oh.my.shipper.core.factories;

import lombok.Data;
import oh.my.shipper.core.task.ShipperTask;

/**
 * task商品
 * 通过 taskImplClazz 反射获取一个task实例
 */
@Data
public class TaskImplStory {
    final Class<?> typeClazz;
    final String type;
    final Class<? extends ShipperTask> taskImplClazz;
}
