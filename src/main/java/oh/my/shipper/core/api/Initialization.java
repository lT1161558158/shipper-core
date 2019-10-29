package oh.my.shipper.core.api;

/**
 * 初始化接口
 * 这个初始化接口实际上将在每次执行时调用
 * 因为filter和output实际上是可以运行时重载参数的
 * 也就是说若需要实现"仅一次""的初始化,则需要在init函数中实现"仅一次"的语义
 */
public interface Initialization {
    void init();
}
