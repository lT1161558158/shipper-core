package top.trister.shipper.core.task;

import lombok.Builder;
import lombok.Data;
import top.trister.shipper.core.api.handler.codec.Codec;
import top.trister.shipper.core.api.handler.input.Input;
import top.trister.shipper.core.api.handler.mapping.Mapping;
import top.trister.shipper.core.api.handler.output.Output;
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
    final DSLDelegate<Mapping> filterDelegate;
    final DSLDelegate<Output> outputDelegate;
    final Codec<?, ?> defaultInputCodec;
    final Codec<?, ?> defaultOutputCodec;
}
