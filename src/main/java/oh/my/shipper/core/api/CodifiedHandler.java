package oh.my.shipper.core.api;

/**
 * 可编码的handler
 * @param <In>
 * @param <Out>
 */
public interface CodifiedHandler<In,Out> extends Handler{
    Codec<In,Out> codec(Codec<In, Out> codec);
    Codec<In,Out> codec();
}
