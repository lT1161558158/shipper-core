package top.trister.shipper.core.dsl;

import groovy.lang.Closure;
import lombok.Data;
import top.trister.shipper.core.api.handler.Handler;

@Data
public class HandlerDefinition<T extends Handler> {
    String name;
    T handler;
    Closure handlerClosure;
    HandlerDelegate handlerDelegate;
}
