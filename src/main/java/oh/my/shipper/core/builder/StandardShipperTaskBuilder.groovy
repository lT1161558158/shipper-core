package oh.my.shipper.core.builder

import oh.my.shipper.core.api.InputCodec
import oh.my.shipper.core.api.OutCodec
import oh.my.shipper.core.dsl.DSLDelegate
import oh.my.shipper.core.dsl.HandlerDefinition
import oh.my.shipper.core.enums.HandlerEnums
import oh.my.shipper.core.exception.ShipperException
import oh.my.shipper.core.implHandler.codec.JsonCodec
import oh.my.shipper.core.implHandler.codec.SimpleCodec
import oh.my.shipper.core.task.ShipperTask
import oh.my.shipper.core.task.StandardShipperTask

import java.util.stream.Collectors

class StandardShipperTaskBuilder implements ShipperTaskBuilder {
    private InputCodec<?> defaultInputCodec
    private OutCodec<?> defaultOutputCodec

    StandardShipperTaskBuilder(InputCodec<?> defaultInputCodec, OutCodec<?> defaultOutputCodec) {
        this.defaultInputCodec = defaultInputCodec
        this.defaultOutputCodec = defaultOutputCodec
    }

    StandardShipperTaskBuilder() {
        defaultInputCodec = new SimpleCodec()
        defaultOutputCodec = new JsonCodec()
    }

    private ShipperTask buildFuture(HandlerDefinition input, DSLDelegate filterDelegate, DSLDelegate outputDelegate) {
        return StandardShipperTask.builder()
                .input(input)
                .filterDelegate(filterDelegate)
                .outputDelegate(outputDelegate)
                .name(input.getName())
                .defaultInputCodec(defaultInputCodec)
                .defaultOutputCodec(defaultOutputCodec)
                .build()
    }

    List<Runnable> builderTask(Map<HandlerEnums, DSLDelegate> dsls) {
        DSLDelegate inputDelegate = dsls.get(HandlerEnums.INPUT)
        DSLDelegate outputDelegate = dsls.get(HandlerEnums.OUTPUT)
        DSLDelegate filterDelegate = dsls.get(HandlerEnums.FILTER)
        if (outputDelegate == null)
            throw new ShipperException("at least one output must be provided")
        if (inputDelegate == null)
            throw new ShipperException("at least one input must be provided")
        inputDelegate.getClosure().call()//创建input的上下文
        List<HandlerDefinition> handlerDefinitions = inputDelegate.getHandlerDefinitions()
        return handlerDefinitions.stream()
                .map({ handlerDefinition -> buildFuture(handlerDefinition, filterDelegate, outputDelegate) })
                .collect(Collectors.toList())

    }
}
