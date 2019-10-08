package oh.my.shipper.core.implHandler;

import lombok.Getter;
import oh.my.shipper.core.api.Codec;
import oh.my.shipper.core.api.CodifiedHandler;

/**
 *  一个简单的实现了codec的赋值的基类
 * @param <In>
 * @param <Out>
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
