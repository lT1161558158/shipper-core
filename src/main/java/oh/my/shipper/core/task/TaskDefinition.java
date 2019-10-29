package oh.my.shipper.core.task;

import lombok.Builder;
import lombok.Data;
import oh.my.shipper.core.api.*;
import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.dsl.HandlerDefinition;
@Data
@Builder
public class TaskDefinition {
    HandlerDefinition<Input> input;
    DSLDelegate<Filter> filterDelegate;
    DSLDelegate<Output> outputDelegate;
    InputCodec<?> defaultInputCodec;
    OutCodec<?> defaultOutputCodec;
}
