package top.trister.shipper.core.implHandler;

import top.trister.shipper.core.api.Scheduled;
import top.trister.shipper.core.exception.ShipperException;
import top.trister.shipper.core.util.CronExpression;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SimpleScheduled implements Scheduled {
    private CronExpression cronExpression;
    private final String cron;
    private long before;

    public SimpleScheduled(String cron) {
        this.cron = cron;
    }

    @Override
    public String cron() {
        return cron;
    }

    @Override
    public boolean trigger() throws InterruptedException {
        if (cronExpression == null) {
            try {
                cronExpression = new CronExpression(cron);
            } catch (ParseException e) {
                throw new ShipperException(e);
            }
        }
        long now = System.currentTimeMillis();
        if (now - before < 1000)
            TimeUnit.MILLISECONDS.sleep(1000 - (now - before));//至少等待一秒才进行下一次调度
        before = System.currentTimeMillis();
        return cronExpression.isSatisfiedBy(new Date());
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }
}
