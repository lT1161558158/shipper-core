package oh.my.shipper.core.enums;

public enum HandlerEnums {
    INPUT("input"),
    FILTER("filter"),
    CODEC("codec"),
    OUTPUT("output")
    ;
    String name;

    HandlerEnums(String name) {
        this.name = name;
    }
}
