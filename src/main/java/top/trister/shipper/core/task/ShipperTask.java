package top.trister.shipper.core.task;

import java.util.List;
import java.util.Map;

public interface ShipperTask extends Runnable {
    /**
     * @return 当前异常列表
     */
    List<Exception> exceptions();

    /**
     * @return task 的状态
     */
    TaskStepEnum state();

    /**
     * 当前task中的event
     *
     * @return event list
     */
    List<Map> nowEvents();
}
