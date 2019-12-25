package top.trister.shipper.core.implHandler;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import top.trister.shipper.core.api.handler.CodifiedHandler;
import top.trister.shipper.core.api.handler.input.CodecInput;

/**
 * 直接返回一个给定的值
 */
@Data
public class IdentityInput implements CodecInput<Object, Object> {
    /**
     * 给定的值
     */
    @Setter
    @Getter
    Object identity;
    /**
     * 使用 SimpleCodifiedHandler 作为 CodifiedHandler的代理
     */
    @Delegate
    CodifiedHandler<Object, Object> simpleCodifiedHandler = new SimpleCodifiedHandler<>();

    @Override
    public Object read() {
        return identity;
    }

}
