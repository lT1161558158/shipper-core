package oh.my.shipper.core.dsl;

import groovy.lang.Closure;

public interface BaseShipper {
    //处理 input 的域
    Object input(Closure closure);
    //处理 filter 的域
    Object filter(Closure closure);
    //处理 output 的域,一般情况下也由output开启整个处理链
    Object output(Closure closure);
}
