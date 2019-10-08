package oh.my.shipper.core.context;

import oh.my.shipper.core.api.Handler;

import java.util.List;
import java.util.Map;

public class HandlerContext {
    Handler handler;
    List<Object> tags;
    Map<String,Object> attr;
}
