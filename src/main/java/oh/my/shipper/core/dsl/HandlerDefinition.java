package oh.my.shipper.core.dsl;

import groovy.lang.Closure;
import lombok.Data;
import oh.my.shipper.core.api.Handler;

@Data
public class HandlerDefinition<T extends Handler> {
    String name;
    T handler;
    Closure handlerClosure;
}
