package oh.my.shipper.core.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 单次读取输入
 */
public interface Input<In> extends CodifiedHandler<In,Map> {

    /**
     * 阻塞的读取一个事件,阻塞参数为-1
     * @return 一个事件
     */
    default Map read(){//默认情况下传入-1的超时时间表示不超时
        return read(TimeUnit.MINUTES,-1);
    }

    /**
     * 可超时的读取一个事件,到达超时时间时将抛出一个运行时异常
     * 大部分情况下应该抛出  {@link oh.my.shipper.core.exception.ShipperException}
     * @param unit 时间单位
     * @param timeout 超时时间
     * @return 一个事件
     */
    Map read(TimeUnit unit, long timeout);

}
