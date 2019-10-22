package oh.my.shipper.core.api;

/**
 * 可编码的handler
 * @param <In> 输入类型
 * @param <Out> 输出类型
 */
public interface CodifiedHandler<In,Out> extends Handler{
    Codec<In,Out> codec(Codec<In, Out> codec);
    Codec<In,Out> codec();
}
