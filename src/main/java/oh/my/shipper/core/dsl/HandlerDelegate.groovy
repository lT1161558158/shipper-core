package oh.my.shipper.core.dsl

import oh.my.shipper.core.api.Handler
import oh.my.shipper.core.builder.HandlerBuilder

/***
 * 解析器的描述和代理类
 */
class HandlerDelegate {
    Handler handler
    Map<String,Object> otherProperties=[:]
    HandlerBuilder handlerBuilder

    HandlerDelegate(Handler handler, HandlerBuilder handlerBuilder) {
        this.handler = handler
        this.handlerBuilder = handlerBuilder
    }

    def methodMissing(String name, Object arg) {//代理不存在的方法调用
        handler."$name"=getArgs(arg)
    }

    def propertyMissing(String name, Object value) {//代理无属性的setter方法
        if(handler.metaClass.hasProperty(handler,name))
            handler."$name"=getArgs(value)
        else
            otherProperties[name]=getArgs(value)
    }

    def codec(String name){
        if (handler.metaClass.respondsTo(handler,"codec")) {
            def codecHandler = handlerBuilder.builderHandler(name)
            handler.codec(codecHandler)
        }
    }
    def getArgs(Object arg){
        if (arg instanceof Object[]){
            return arg[0]
        }
    }
}
