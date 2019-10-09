package oh.my.shipper.core.dsl

import oh.my.shipper.core.enums.HandlerEnums

abstract class BaseShipperScript extends Script implements BaseShipper {

    Map<HandlerEnums, DSLDelegate> handlerMap = [:]

    def dslParse(Closure closure, HandlerEnums handler) {
        def delegate = new DSLDelegate()
        delegate.handlerBuilder = handlerBuilder
        delegate.closure = closure
        closure.delegate = delegate
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        handlerMap[handler] = delegate
    }

    @Override
    def input(Closure closure) {
        dslParse(closure, HandlerEnums.INPUT)
    }

    @Override
    def filter(Closure closure) {
        dslParse(closure, HandlerEnums.FILTER)
    }

    @Override
    def output(Closure closure) {
        dslParse(closure, HandlerEnums.OUTPUT)
        handlerExecutor.execute(handlerMap)
    }

}
