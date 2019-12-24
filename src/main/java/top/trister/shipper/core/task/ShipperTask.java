package top.trister.shipper.core.task;

import top.trister.shipper.core.exception.ShipperException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public interface ShipperTask extends Callable<ShipperTask> {
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

    default ShipperTask doing(){
        try{
            return call();
        }catch (Exception e){
            throw new ShipperException(e);
        }
    }
}
