package top.trister.shipper.core.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.trister.shipper.core.api.Input;
import top.trister.shipper.core.api.Scheduled;
import top.trister.shipper.core.dsl.HandlerDefinition;
import top.trister.shipper.core.util.CronExpression;
import top.trister.shipper.core.exception.ShipperException;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
@Data
public class StandardScheduleShipperTask extends StandardSimpleShipperTask implements ScheduleShipperTask {
    private CronExpression cronExpression;
    private Scheduled scheduled;

    @Override
    protected Input initInput(HandlerDefinition<Input> input) {
        Input handler = super.initInput(input);
        if (!(handler instanceof Scheduled))
            throw new ShipperException("input " + handler.getClass().getSimpleName() + " is not " + Scheduled.class.getSimpleName());
        scheduled = ((Scheduled) handler);
        String cron = scheduled.cron();
        try {
            cronExpression = new CronExpression(cron);
        } catch (ParseException e) {
            throw new ShipperException(e);
        }
        return handler;
    }

    @Override
    public boolean trigger() {
        return cronExpression.isSatisfiedBy(new Date());
    }

    @Override
    protected void doSomething() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted() && !scheduled.isInterrupted()) {
            if (trigger()) {
                long start = System.currentTimeMillis();
                super.doSomething();
                long end = System.currentTimeMillis();
                if (end - start < 1000)
                    TimeUnit.MILLISECONDS.sleep(1000 - (end - start));//至少等待一秒才进行下一次调度
            } else {
                TimeUnit.SECONDS.sleep(1);
            }
        }

    }
}
