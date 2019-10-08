package oh.my.shipper.core.dsl;

import groovy.lang.Closure;
import lombok.Data;
import oh.my.shipper.core.api.Handler;

@Data
public class HandlerDefinition {
    String name;
    Handler handler;
    Closure handlerClosure;
}
