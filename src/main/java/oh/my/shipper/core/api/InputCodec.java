package oh.my.shipper.core.api;

import java.util.Map;

/**
 *  从Object编码为Event对象
 * @param <In>
 */
public interface InputCodec<In> extends Codec<In, Map> {

}
