package top.trister.shipper.core.api.handler.input;

import top.trister.shipper.core.api.handler.CodifiedHandler;

/**
 * 包含编码器的 input
 *
 * @param <In>  原始输入
 * @param <Out> 编码后的输入
 */
public interface CodecInput<In, Out> extends Input<In>, CodifiedHandler<In, Out> {
    default Out codecRead() {
        return codec().codec(read());
    }
}
