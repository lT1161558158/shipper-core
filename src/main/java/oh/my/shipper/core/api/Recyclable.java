package oh.my.shipper.core.api;

/**
 * 可重复的
 */
public interface Recyclable extends Interrupted {
    /**
     *
     * @return 是否还可循环
     */
    boolean recyclable();
}
