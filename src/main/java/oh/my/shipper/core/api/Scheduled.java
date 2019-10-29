package oh.my.shipper.core.api;

/**
 * 可调度的
 * 返回一个cron表达式
 */
public interface Scheduled extends Interrupted {
    String cron();
}
