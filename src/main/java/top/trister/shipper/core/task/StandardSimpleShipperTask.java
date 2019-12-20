package top.trister.shipper.core.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import top.trister.shipper.core.api.Input;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class StandardSimpleShipperTask extends AbstractShipperTask {
    @Override
    protected void doSomething() throws InterruptedException {
        Input input = initInput(taskDefinition.getInput());
        log.debug("{} waiting for event",input);
        Map event = input.read();
        List<Map> maps = doFilter(taskDefinition.getFilterDelegate(), event);
        doOutPut(taskDefinition.getOutputDelegate(),maps);
    }
}
