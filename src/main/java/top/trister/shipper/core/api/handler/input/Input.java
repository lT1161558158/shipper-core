package top.trister.shipper.core.api.handler.input;

import top.trister.shipper.core.api.handler.Handler;

import java.util.function.Supplier;

/**
 * 单次读取输入
 */
@FunctionalInterface
public interface Input<In> extends Supplier<In>, Handler {

    /**
     * 阻塞的读取一个事件,阻塞参数为-1
     *
     * @return 一个事件
     */
    In read();

    @Override
    default In get() {
        return read();
    }
}
