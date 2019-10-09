package oh.my.shipper.core.dsl

import oh.my.shipper.core.enums.HandlerEnums

abstract class BaseCollectorScript extends Script {

    Map<HandlerEnums, DSLDelegate> handlerMap = [:]
    def dslParse(Closure closure, HandlerEnums handler){
        def delegate = new DSLDelegate()
        delegate.handlerBuilder=handlerBuilder
        delegate.closure = closure
        closure.delegate = delegate
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        handlerMap[handler] = delegate
    }

    def input(Closure closure) {
        dslParse(closure, HandlerEnums.INPUT)
    }

    def filter(Closure closure) {
        dslParse(closure,HandlerEnums.FILTER)
    }

    def output(Closure closure) {
        dslParse(closure,HandlerEnums.OUTPUT)
        handlerExecutor.execute(handlerMap)
    }

}
