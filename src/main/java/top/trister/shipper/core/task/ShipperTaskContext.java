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
public class ShipperTaskContext {
    final HandlerDefinition<Input> input;
    final DSLDelegate<Filter> filterDelegate;
    final DSLDelegate<Output> outputDelegate;
    final InputCodec<?> defaultInputCodec;
    final OutCodec<?> defaultOutputCodec;
}
