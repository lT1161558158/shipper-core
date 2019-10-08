package oh.my.shipper.core.api;

import java.util.List;
import java.util.Map;

/**
 * 过滤器
 */
public interface Filter extends Handler {
    /**
     *
     * @param event 输入事件
     */
    List<Map> filter(Map event);
}
