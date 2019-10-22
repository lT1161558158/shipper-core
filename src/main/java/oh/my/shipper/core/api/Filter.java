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
     * @return 返回应用了filter后的事件集合
     */
    List<Map> filter(Map event);
}
