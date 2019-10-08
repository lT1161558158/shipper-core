package oh.my.shipper.core.dsl


class PropertiesDelegate {
    Map<String, Object> properties = [:]

    def methodMissing(String name, Object arg) {//代理不存在的方法调用
        Object[] args = arg
        properties[name] = args[0]
    }

    def propertyMissing(String name, Object value) {//代理无属性的setter方法
        properties[name] = value
    }

    def propertyMissing(String name) {//代理无属性的getter方法
        properties[name]
    }

    @Override
    String toString() {
        return "${this.class} [ properties: $properties]"
    }
}

