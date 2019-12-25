package top.trister.shipper.core.api.handler.output;

import top.trister.shipper.core.api.handler.Handler;

import java.util.function.Consumer;

/**
 * 输出
 */
public interface Output<Out> extends Consumer<Out>, Handler {

    /**
     * @param event 写入一个事件
     */
    void write(Out event);

    @Override
    default void accept(Out out){
        write(out);
    }
}
