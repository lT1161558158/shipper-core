package top.trister.shipper.core.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.trister.shipper.core.api.Scheduled;
import top.trister.shipper.core.api.handler.Handler;
import top.trister.shipper.core.api.handler.codec.Codec;
import top.trister.shipper.core.api.handler.input.Input;
import top.trister.shipper.core.api.handler.mapping.Mapping;
import top.trister.shipper.core.api.handler.output.Output;
import top.trister.shipper.core.bean.Shipper;
import top.trister.shipper.core.dsl.DSLDelegate;
import top.trister.shipper.core.dsl.HandlerDefinition;
import top.trister.shipper.core.enums.HandlerEnums;
import top.trister.shipper.core.exception.ShipperException;
import top.trister.shipper.core.factories.TaskFactory;
import top.trister.shipper.core.implHandler.SimpleScheduledInput;
import top.trister.shipper.core.implHandler.codec.JsonCodec;
import top.trister.shipper.core.implHandler.codec.SimpleCodec;
import top.trister.shipper.core.task.ShipperTask;
import top.trister.shipper.core.task.ShipperTaskContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class StandardShipperTaskBuilder implements ShipperTaskBuilder {
    private static final Logger log = LoggerFactory.getLogger(StandardShipperTaskBuilder.class);
    private Codec<?, ?> defaultInputCodec;
    private Codec<?, ?> defaultOutputCodec;
    private TaskFactory taskFactory = new TaskFactory();

    public StandardShipperTaskBuilder(Codec<?, ?> defaultInputCodec, Codec<?, ?> defaultOutputCodec) {
        this.defaultInputCodec = defaultInputCodec;
        this.defaultOutputCodec = defaultOutputCodec;
    }

    public StandardShipperTaskBuilder() {
        defaultInputCodec = new SimpleCodec();
        defaultOutputCodec = new JsonCodec();
    }

    private ShipperTask buildTask(HandlerDefinition<Input> input, DSLDelegate<Mapping> filterDelegate, DSLDelegate<Output> outputDelegate) {

        ShipperTaskContext shipperTaskContext = ShipperTaskContext.builder()
                .input(input)
                .filterDelegate(filterDelegate)
                .outputDelegate(outputDelegate)
                .defaultInputCodec(defaultInputCodec)
                .defaultOutputCodec(defaultOutputCodec)
                .build();
        Handler handler = input.getHandler();
        try {
            ShipperTask shipperTask;
            String cron = input.getHandlerDelegate().cron();
            if (cron != null && !(handler instanceof Scheduled)) {
                input.setHandler(new SimpleScheduledInput(input.getHandler(), cron));//给simple类型的shipper添加cron能力
                shipperTask = taskFactory.findStory(Scheduled.class).getTaskImplClazz().newInstance();
            } else {
                shipperTask = taskFactory.findStory(handler.getClass()).getTaskImplClazz().newInstance();
            }
            log.debug("build {}", shipperTask.getClass().getSimpleName());
            shipperTask.shipperTaskContext(shipperTaskContext);
            shipperTask.log(LoggerFactory.getLogger(nameBuilder(input, filterDelegate, outputDelegate)));
            return shipperTask;
        } catch (Exception e) {
            throw new ShipperException(e);
        }

    }

    private String nameBuilder(HandlerDefinition input, DSLDelegate<Mapping> filterDelegate, DSLDelegate<Output> outputDelegate) {

        StringBuilder builder = new StringBuilder(input.getName());
        if (filterDelegate != null) {

            filterDelegate.getClosure().call();
            filterDelegate.getAndClear().forEach(h -> builder.append("|").append(h.getName()));
        }

        outputDelegate.getClosure().call();
        StringBuilder outBuilder = new StringBuilder();

        outputDelegate.getAndClear().forEach(h -> {
            if (outBuilder.length() != 0)
                outBuilder.append(",");
            outBuilder.append(h.getName());
        });
        builder.append("|").append("[").append(outBuilder).append("]");
        return builder.toString();
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<ShipperTask> build(Shipper shipper) {
        Map<HandlerEnums, DSLDelegate> context = shipper.getContext();
        DSLDelegate inputDelegate = context.get(HandlerEnums.INPUT);
        DSLDelegate outputDelegate = context.get(HandlerEnums.OUTPUT);
        DSLDelegate filterDelegate = context.get(HandlerEnums.FILTER);
        if (outputDelegate == null)
            throw new ShipperException("at least one output must be provided");
        if (inputDelegate == null)
            throw new ShipperException("at least one input must be provided");
        inputDelegate.getClosure().call();//创建input的上下文;
        Map<String, HandlerDefinition<Input>> handlerDefinitions = inputDelegate.getHandlerDefinitions();
        return handlerDefinitions.values().stream()
                .map(e -> buildTask(e, filterDelegate, outputDelegate))
                .collect(Collectors.toList());
    }

    ;
};
