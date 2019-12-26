package top.trister.shipper.core.implHandler;

import lombok.experimental.Delegate;
import top.trister.shipper.core.api.Initialization;
import top.trister.shipper.core.api.handler.input.Input;
import top.trister.shipper.core.api.handler.input.ScheduledInput;
import top.trister.shipper.core.exception.ShipperException;
import top.trister.shipper.core.util.CronExpression;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SimpleScheduledInput implements ScheduledInput, Initialization {
    @Delegate
    private final Input input;
    private CronExpression cronExpression;
    private final String cron;
    private long before;

    public SimpleScheduledInput(Input input, String cron) {
        this.input = input;
        this.cron = cron;
    }

    @Override
    public String cron() {
        return cron;
    }

    @Override
    public boolean trigger() throws InterruptedException {
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

    @Override
    public void init() {
        if (cronExpression == null) {
            try {
                cronExpression = new CronExpression(cron);
            } catch (ParseException e) {
                throw new ShipperException(e);
            }
        }
    }
}
