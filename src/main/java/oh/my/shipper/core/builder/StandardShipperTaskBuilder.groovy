package oh.my.shipper.core.builder

import oh.my.shipper.core.api.*
import oh.my.shipper.core.bean.Shipper
import oh.my.shipper.core.dsl.DSLDelegate
import oh.my.shipper.core.dsl.HandlerDefinition
import oh.my.shipper.core.enums.HandlerEnums
import oh.my.shipper.core.exception.ShipperException
import oh.my.shipper.core.factories.TaskFactory
import oh.my.shipper.core.factories.TaskImplStory
import oh.my.shipper.core.implHandler.codec.JsonCodec
import oh.my.shipper.core.implHandler.codec.SimpleCodec
import oh.my.shipper.core.task.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

class StandardShipperTaskBuilder implements ShipperTaskBuilder {
    private static final Logger log = LoggerFactory.getLogger(StandardShipperTaskBuilder.class)
    private InputCodec<?> defaultInputCodec
    private OutCodec<?> defaultOutputCodec

    TaskFactory taskFactory = new TaskFactory()

    StandardShipperTaskBuilder(InputCodec<?> defaultInputCodec, OutCodec<?> defaultOutputCodec) {
        this.defaultInputCodec = defaultInputCodec
        this.defaultOutputCodec = defaultOutputCodec
    }

    StandardShipperTaskBuilder() {
        defaultInputCodec = new SimpleCodec()
        defaultOutputCodec = new JsonCodec()
    }

    private ShipperTask buildFuture(HandlerDefinition input, DSLDelegate filterDelegate, DSLDelegate outputDelegate) {
        def taskDefinition = TaskDefinition.builder()
                .input(input)
                .filterDelegate(filterDelegate)
                .outputDelegate(outputDelegate)
                .defaultInputCodec(defaultInputCodec)
                .defaultOutputCodec(defaultOutputCodec)
                .build()
        Handler handler = input.getHandler()
        TaskImplStory taskImplStory = taskFactory.findStory(handler.class)
        ShipperTask shipperTask = taskImplStory.taskImplClazz.newInstance()
        log.debug("build {}", taskImplStory.taskImplClazz)
        shipperTask.taskDefinition(taskDefinition)
        return shipperTask
    }

    @Override
    List<ShipperTask> build(Shipper shipper) {
        Map<HandlerEnums, DSLDelegate> context = shipper.getContext()
        DSLDelegate inputDelegate = context.get(HandlerEnums.INPUT)
        DSLDelegate outputDelegate = context.get(HandlerEnums.OUTPUT)
        DSLDelegate filterDelegate = context.get(HandlerEnums.FILTER)
        if (outputDelegate == null)
            throw new ShipperException("at least one output must be provided")
        if (inputDelegate == null)
            throw new ShipperException("at least one input must be provided")
        inputDelegate.getClosure().call()//创建input的上下文
        Map<String, HandlerDefinition> handlerDefinitions = inputDelegate.getHandlerDefinitions()
        return handlerDefinitions.values().stream()
                .map({ e -> buildFuture(e, filterDelegate, outputDelegate) })
                .collect(Collectors.toList())
    }
}
