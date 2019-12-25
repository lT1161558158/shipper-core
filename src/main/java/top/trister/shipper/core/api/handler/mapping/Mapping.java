package top.trister.shipper.core.api.handler.mapping;

import top.trister.shipper.core.api.handler.Handler;

import java.util.function.Function;

/**
 * 映射器
 */
@FunctionalInterface
public interface Mapping<T,R> extends Handler, Function<T,R> {
    /**
     * @param t 输入
     * @return 映射后的结果
     */
    R mapping(T t);

    @Override
    default R apply(T t){
        return mapping(t);
    }
}
