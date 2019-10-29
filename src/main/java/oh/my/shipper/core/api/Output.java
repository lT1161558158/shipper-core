package oh.my.shipper.core.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 输出
 */
public interface Output<Out> extends CodifiedHandler<Map,Out> {


    /**
     *
     * @param event 写入一个事件
     */
    default void write(Map event){
        write(event,TimeUnit.MINUTES,-1);
    }

    /**
     *
     * @param event 事件
     * @param unit 时间单位
     * @param timeout 超时时间
     */
    void write(Map event, TimeUnit unit, long timeout);

}
