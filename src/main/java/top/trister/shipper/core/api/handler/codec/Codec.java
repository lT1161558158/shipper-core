package top.trister.shipper.core.api.handler.codec;

import top.trister.shipper.core.api.handler.mapping.Mapping;

/**
 * 编码器
 * 编码器是一种特殊的映射器
 */
@FunctionalInterface
public interface Codec<T,R> extends Mapping<T,R> {
    @Override
    default R mapping(T t){
        return codec(t);
    }

    /**
     * 编码相应的对象
     * @param t 输入类型
     * @return 输出类型
     */
    R codec(T t);
}
