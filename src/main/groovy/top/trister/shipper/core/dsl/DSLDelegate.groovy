package top.trister.shipper.core.dsl

import top.trister.shipper.core.api.Handler
import top.trister.shipper.core.builder.HandlerBuilder

class DSLDelegate<T extends Handler> extends PropertiesDelegate {

    Map<String,HandlerDefinition<T>> handlerDefinitions=[:]
    Closure closure
    HandlerBuilder handlerBuilder
    long timeout = -1

    def methodMissing(String name, Object obj) {
        Object[] args = obj
        for (arg in args) {
            if (arg instanceof Closure) {
                if (!handlerDefinitions.containsKey(name)){
                    def handler = handlerBuilder.builderHandler(name)
                    def definition = new HandlerDefinition()
                    Closure closure = arg
                    def delegate = new HandlerDelegate(handler, handlerBuilder)
                    definition.handlerClosure = closure
                    definition.handler = handler
                    definition.name = name
                    closure.delegate = delegate
                    closure.resolveStrategy = Closure.DELEGATE_ONLY
                    handlerDefinitions[name]=definition
                }
            } else {
                Object.methodMissing(name, arg)
            }
        }
    }
    @Override
    String toString() {
        return "${this.class} [ handlerDefinitions :$handlerDefinitions]"
    }
}

