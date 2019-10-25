package oh.my.shipper.core.dsl

import oh.my.shipper.core.api.Handler
import oh.my.shipper.core.builder.HandlerBuilder

/***
 * 解析器的描述和代理类
 */
class HandlerDelegate {
    Handler handler
    HandlerBuilder handlerBuilder

    HandlerDelegate(Handler handler, HandlerBuilder handlerBuilder) {
        this.handler = handler
        this.handlerBuilder = handlerBuilder
    }

    def methodMissing(String name, Object arg) {//代理不存在的方法调用
        propertyMissing(name,arg)
    }

    def propertyMissing(String name, Object value) {//代理无属性的setter方法
        if(handler.metaClass.hasProperty(handler,name))
            handler."$name"=getArgs(value)
    }

    def codec(String name){
        if (handler.metaClass.respondsTo(handler,"codec")) {
            def codecHandler = handlerBuilder.builderHandler(name)
            handler.codec(codecHandler)
        }
    }
    def codec(Closure closure){
        if (handler.metaClass.respondsTo(handler,"codec")) {
            closure.delegate = new PropertiesDelegate()
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure()
            String name = closure.delegate.properties.get("\$")
            def codecHandler = handlerBuilder.builderHandler(name,closure.delegate.properties)
            handler.codec(codecHandler)
        }
    }

    static def getArgs(Object arg){
        if (arg instanceof Object[]){
            return arg[0]
        }else{
            return arg
        }
    }
}
