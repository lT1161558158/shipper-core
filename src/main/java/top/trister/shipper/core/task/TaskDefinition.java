package top.trister.shipper.core.task;

import lombok.Builder;
import lombok.Data;
import top.trister.shipper.core.api.*;
import top.trister.shipper.core.dsl.DSLDelegate;
import top.trister.shipper.core.dsl.HandlerDefinition;

/**
 * task的描述信息
 * {@link ShipperTask}
 */
@Data
@Builder
public class TaskDefinition {
    HandlerDefinition<Input> input;
    DSLDelegate<Filter> filterDelegate;
    DSLDelegate<Output> outputDelegate;
    InputCodec<?> defaultInputCodec;
    OutCodec<?> defaultOutputCodec;
}
