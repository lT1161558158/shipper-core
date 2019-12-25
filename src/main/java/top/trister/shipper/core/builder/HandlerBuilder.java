package top.trister.shipper.core.builder;

import top.trister.shipper.core.api.handler.Handler;

public interface HandlerBuilder {
    /**
     * 加载Handler
     */
    void reLoadHandler();

    /**
     * @param name 处理器名字
     * @return 构造出的实例
     */
    Handler builderHandler(String name);
}
