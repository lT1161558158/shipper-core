package top.trister.shipper.core.implHandler;

import lombok.Getter;
import top.trister.shipper.core.api.Codec;
import top.trister.shipper.core.api.CodifiedHandler;

/**
 *  一个简单的实现了codec的赋值的基类
 * @param <In> 输入类型
 * @param <Out> 输出类型
 */
public abstract class SimpleCodifiedHandler<In, Out> implements CodifiedHandler<In, Out> {
    @Getter
    protected Codec<In, Out> codec;
    @Override
    public Codec<In, Out> codec(Codec<In, Out> codec) {
        this.codec = codec;
        return this.codec;
    }

    @Override
    public Codec<In, Out> codec() {
        return this.codec;
    }
}
