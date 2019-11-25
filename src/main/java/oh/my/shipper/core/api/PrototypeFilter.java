package oh.my.shipper.core.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对于那些不会分裂event的filter的接口
 */
public interface PrototypeFilter extends Filter {
    @Override
    default List<Map> filter(Map event){
        return Collections.singletonList(prototypeFilter(event));
    }
    Map prototypeFilter(Map event);
}
