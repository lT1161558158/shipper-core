package top.trister.shipper.core.api.handler.output;

import top.trister.shipper.core.api.handler.CodifiedHandler;

/**
 * 可编码的输出
 * @param <In> 原始输出
 * @param <Out> 编码后的输出
 */
public interface CodecOutput<In,Out> extends Output<Out>, CodifiedHandler<In, Out> {

    default void codecWrite(In out){
        write(codec().apply(out));
    }
}
