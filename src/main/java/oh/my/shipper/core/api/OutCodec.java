package oh.my.shipper.core.api;

import java.util.Map;

/**
 *  从 Event 编码为 Object 对象
 * @param <Out> 输出类型
 */
public interface OutCodec<Out> extends Codec<Map,Out> {

}
