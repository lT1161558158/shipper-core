package top.trister.shipper.core.task;

import java.util.List;

public interface ShipperTask extends Runnable,ShipperTaskContextAware,LogAware {
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
    Object nowEvents();

    default ShipperTask doing(){
        run();
        return this;
    }

}
