package oh.my.shipper.core.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 输入
 */
public interface Input<In> extends AutoCloseable,CodifiedHandler<In,Map> {

    /**
     *
     * @return 一个事件
     */
    default Map read(){//默认情况下传入-1的超时时间表示不超时
        return read(TimeUnit.MINUTES,-1);
    }

    /**
     *
     * @param unit 时间单位
     * @param timeout 超时时间
     * @return 一个事件
     */
    Map read(TimeUnit unit, long timeout);

    /**
     *
     * @return 是否可读事件
     */
    boolean ready();

}
