package oh.my.shipper.core.builder;

import oh.my.shipper.core.api.Handler;

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
