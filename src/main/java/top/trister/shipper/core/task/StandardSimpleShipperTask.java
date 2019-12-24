package top.trister.shipper.core.task;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StandardSimpleShipperTask extends AbstractShipperTask {
    @Override
    protected void doSomething() throws InterruptedException {
        doInput().doFilter().doOutPut();
    }
}
