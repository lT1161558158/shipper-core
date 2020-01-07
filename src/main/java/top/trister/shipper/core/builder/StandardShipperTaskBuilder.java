package top.trister.shipper.core.builder;

import lombok.extern.slf4j.Slf4j;
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
import top.trister.shipper.core.implHandler.SimpleScheduled;
import top.trister.shipper.core.implHandler.codec.JsonCodec;
import top.trister.shipper.core.implHandler.codec.SimpleCodec;
import top.trister.shipper.core.task.ShipperTask;
import top.trister.shipper.core.task.ShipperTaskContext;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class StandardShipperTaskBuilder implements ShipperTaskBuilder {
    /**
     * 默认的输入编解码器
     */
    private Codec<?, ?> defaultInputCodec;
    /**
     * 默认的输出编解码器
     */
    private Codec<?, ?> defaultOutputCodec;
    /**
     * 任务工厂
     */
    private TaskFactory taskFactory = new TaskFactory();

    public StandardShipperTaskBuilder(Codec<?, ?> defaultInputCodec, Codec<?, ?> defaultOutputCodec) {
        this.defaultInputCodec = defaultInputCodec;
        this.defaultOutputCodec = defaultOutputCodec;
    }

    public StandardShipperTaskBuilder() {
        defaultInputCodec = new SimpleCodec();
        defaultOutputCodec = new JsonCodec();
    }

    /**
     * @param inputDSLDelegate inputDSLDelegate
     * @param input            input
     * @param filterDelegate   filterDelegate
     * @param outputDelegate   outputDelegate
     * @return ShipperTask
     */
    private ShipperTask buildTask(DSLDelegate<Input> inputDSLDelegate, HandlerDefinition<Input> input, DSLDelegate<Mapping> filterDelegate, DSLDelegate<Output> outputDelegate) {

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
            String cron = inputDSLDelegate.cron();
            if (cron != null && !(handler instanceof Scheduled)) {
                Class<?>[] interfaces = handler.getClass().getInterfaces();
                Class[] newInterFaces = new Class[interfaces.length + 1];
                System.arraycopy(interfaces, 0, newInterFaces, 0, interfaces.length);
                newInterFaces[interfaces.length] = Scheduled.class;
                Scheduled scheduled = new SimpleScheduled(cron);
                Set<Method> methods = Stream.of(Scheduled.class.getMethods()).collect(Collectors.toSet());
                Input newHandler = (Input) Proxy.newProxyInstance(handler.getClass().getClassLoader(), newInterFaces, (proxy, method, args) -> method.invoke(methods.contains(method) ? scheduled : handler, args));
                input.setHandler(newHandler);//给simple类型的shipper添加cron能力
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

    /**
     * @param input          input
     * @param filterDelegate filterDelegate
     * @param outputDelegate outputDelegate
     * @return 任务的名字
     */
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
        DSLDelegate<Input> inputDelegate = context.get(HandlerEnums.INPUT);
        DSLDelegate<Output> outputDelegate = context.get(HandlerEnums.OUTPUT);
        DSLDelegate<Mapping> filterDelegate = context.get(HandlerEnums.FILTER);
        if (outputDelegate == null)
            throw new ShipperException("at least one output must be provided");
        if (inputDelegate == null)
            throw new ShipperException("at least one input must be provided");
        inputDelegate.getClosure().call();//创建input的上下文;
        Map<String, HandlerDefinition<Input>> handlerDefinitions = inputDelegate.getHandlerDefinitions();
        return handlerDefinitions.values().stream().map(e -> buildTask(inputDelegate, e, filterDelegate, outputDelegate)).collect(Collectors.toList());
    }
}
