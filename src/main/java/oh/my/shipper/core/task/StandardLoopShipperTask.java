package oh.my.shipper.core.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import oh.my.shipper.core.api.Input;
import oh.my.shipper.core.api.Recyclable;
import oh.my.shipper.core.dsl.HandlerDefinition;
import oh.my.shipper.core.exception.ShipperException;


@EqualsAndHashCode(callSuper = true)
@Data
public class StandardLoopShipperTask extends StandardSimpleShipperTask implements LoopShipperTask {

    boolean loop;
    Recyclable recyclable;
    @Override
    protected Input initInput(HandlerDefinition<Input> input) {
        Input handler = super.initInput(input);
        if (handler instanceof Recyclable){
            recyclable=(Recyclable) handler;
            loop = recyclable.recyclable();
        }
        else
            throw new ShipperException("input " + handler.getClass().getSimpleName() + " is not " + Recyclable.class.getSimpleName());
        return handler;
    }

    @Override
    protected void doSomething() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted() && !recyclable.isInterrupted() && loop())
            super.doSomething();
    }

    @Override
    public boolean loop() {
        return loop;
    }
}
