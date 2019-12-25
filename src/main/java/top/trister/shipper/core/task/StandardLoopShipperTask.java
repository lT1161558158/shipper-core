package top.trister.shipper.core.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.trister.shipper.core.api.handler.input.Input;
import top.trister.shipper.core.api.Recyclable;
import top.trister.shipper.core.dsl.HandlerDefinition;
import top.trister.shipper.core.exception.ShipperException;


@EqualsAndHashCode(callSuper = true)
@Data
public class StandardLoopShipperTask extends StandardSimpleShipperTask implements LoopShipperTask {

    private Recyclable recyclable;

    @Override
    protected Input initInput(HandlerDefinition<Input> input) {
        Input handler = super.initInput(input);
        if (!(handler instanceof Recyclable))
            throw new ShipperException("input " + handler.getClass().getSimpleName() + " is not " + Recyclable.class.getSimpleName());
        recyclable = (Recyclable) handler;
        return handler;
    }

    @Override
    protected void doSomething() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted() && !recyclable.isInterrupted() && loop())
            super.doSomething();
    }

    @Override
    public boolean loop() {
        return recyclable.recyclable();
    }
}
