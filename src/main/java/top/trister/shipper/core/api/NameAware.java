package top.trister.shipper.core.api;

/**
 * 名字
 * 暂时无用
 * 后续可以使用该名字作为shipper的执行日志名字
 * 涉及到的修改部分有点多
 */
public interface NameAware {
    /**
     *
     * @param name 默认情况下不能设置名字
     */
    default void name(String name) {
        throw new UnsupportedOperationException("can't set name");
    }

    /**
     *
     * @return 默认情况下名字是当前类名
     */
    default String name() {
        return this.getClass().getSimpleName();
    }
}
