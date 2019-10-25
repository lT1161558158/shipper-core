package oh.my.shipper.core.dsl

import oh.my.shipper.core.api.Handler
import oh.my.shipper.core.builder.HandlerBuilder

class DSLDelegate<T extends Handler> extends PropertiesDelegate {
    List<HandlerDefinition<T>> handlerDefinitions = []
    Closure closure
    HandlerBuilder handlerBuilder
    long timeout = -1

    def methodMissing(String name, Object obj) {
        Object[] args = obj
        for (arg in args) {
            if (arg instanceof Closure && handlerDefinitions.empty) {//第一次才执行创建 handlerDefinition
                def handler = handlerBuilder.builderHandler(name)
                def definition = new HandlerDefinition()
                Closure closure = arg
                def delegate = new HandlerDelegate(handler, handlerBuilder)
                definition.handlerClosure = closure
                definition.handler = handler
                definition.name = name
                closure.delegate = delegate
                closure.resolveStrategy = Closure.DELEGATE_ONLY
                handlerDefinitions << definition
            } else {
                super.methodMissing(name, arg)
            }
        }
    }
    @Override
    String toString() {
        return "${this.class} [ handlerDefinitions :$handlerDefinitions]"
    }
}

