package top.trister.shipper.core.dsl

import top.trister.shipper.core.enums.HandlerEnums

import java.text.SimpleDateFormat

abstract class BaseShipperScript extends Script implements BaseShipper {


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
    }
    def cron(Closure closure){
        //TODO 在layer解析时设置 cron
    }
    //////////////自定义函数
    /**
     *
     * @param format 格式化戳
     * @return 格式化后的当前时间
     */
    static def now(String format){
        return new SimpleDateFormat(format).format(new Date())
    }
}
