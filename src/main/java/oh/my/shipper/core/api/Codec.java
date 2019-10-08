package oh.my.shipper.core.api;

/**
 * 编码器
 */
public interface Codec<In,Out> extends Handler {
    /**
     *
     * @param input 输入事件
     * @return 经过编码后的事件
     */
    Out codec(In input);
}
