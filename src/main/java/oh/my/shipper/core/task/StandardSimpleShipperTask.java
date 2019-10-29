package oh.my.shipper.core.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import oh.my.shipper.core.api.Input;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class StandardSimpleShipperTask extends AbstractShipperTask {
    @Override
    protected void doSomething() throws InterruptedException {
        Input input = initInput(taskDefinition.getInput());
        Map event = input.read();
        List<Map> maps = doFilter(taskDefinition.getFilterDelegate(), event);
        doOutPut(taskDefinition.getOutputDelegate(),maps);
    }
}
