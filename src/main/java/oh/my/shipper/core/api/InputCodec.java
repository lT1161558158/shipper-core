package oh.my.shipper.core.api;

import java.util.Map;

/**
 *  从Object编码为Event对象
 * @param <In> 输入类型
 */
public interface InputCodec<In> extends Codec<In, Map> {

}
