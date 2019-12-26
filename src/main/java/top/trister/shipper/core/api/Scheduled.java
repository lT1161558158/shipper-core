package top.trister.shipper.core.api;

/**
 * 可调度的
 * 可以访问cron表达式
 * 通过trigger确定是否触发
 */
public interface Scheduled extends Interrupted {

    String cron();

    boolean trigger() throws InterruptedException;
}
