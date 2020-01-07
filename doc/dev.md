# shipper

## 基本接口

数据的处理过程可以被描述为3步

1. 输入
2. 映射
3. 输出

这3步分别对应了shipper中的3类接口

1. Input
2. Mapping
3. Output

![1578363699416](.\1578363699416.png)

这3类接口也分别对应了Supplier,Consumer,Function#apply函数

## 编解码器

众所周知,数据是需要经过编解码才能使用的.对应这个,shipper提供了Codec接口作为编解码器

![1578363998993](.\1578363998993.png)

很容易就能看出Codec其实是Mapping的一个特化类型.因为编解码其实就是将数据映射为其他类型.

### 可编解码的handler

为了体现编解码器的特征,新增了一个可编解码的handler接口

![1578364118684](.\1578364118684.png)

为了体现handler是可编解码的,需要实现这个接口.

### 可编解码的input/output

为了体现input/output的可编解码,提供了更好用的input/output.并且默认实现了其中的编解码函数.

![1578364249452](.\1578364249452.png)

因此,实现可编解码的input/output和基本的input/output是一致的,仅需要实现input/output的函数.但由于java语言的限制,无法在接口中添加成员,因此还需要实现CodifiedHandler接口.

实现这个接口可以有两种方式

1. 继承SimpleCodifiedHandler
2. 代理SimpleCodifiedHandler实现

SimpleCodifiedHandler是CodifiedHandler接口的一个简单实现,如下

```java
/**
 *  一个简单的实现了codec的赋值的基类
 * @param <In> 输入类型
 * @param <Out> 输出类型
 */
@Data
public class SimpleCodifiedHandler<In, Out> implements CodifiedHandler<In, Out> {
    @Getter
    protected Codec<In, Out> codec;
    @Override
    public Codec<In, Out> codec(Codec<In, Out> codec) {
        this.codec = codec;
        return this.codec;
    }

    @Override
    public Codec<In, Out> codec() {
        return this.codec;
    }
}
```

仅存在了一个Codec成员,以便codec函数可以将codec设置其中.

继承实现的方式没什么好说的.

代理实现的方式可以借助lombok.例子如下

```java
/**
 * 直接返回一个给定的值
 */
@Data
public class IdentityInput implements CodecInput<Object, Object> {
    /**
     * 给定的值
     */
    @Setter
    @Getter
    Object identity;
    /**
     * 使用 SimpleCodifiedHandler 作为 CodifiedHandler的代理
     */
    @Delegate
    CodifiedHandler<Object, Object> simpleCodifiedHandler = new SimpleCodifiedHandler<>();

    @Override
    public Object read() {
        return identity;
    }

}
```

上例中,IdentityInput实现了CodecInput接口,但没有实现其中的函数,而是使用@Delegate注解在了SimpleCodifiedHandler成员上,以表明使用SimpleCodifiedHandler作为未实现方法的实现.

## 运行

### BootStrapShipper

在shipper.core中提供了BootStrapShipper类以简单的使用shipper.

在该类中可以看到使用的标准形式.

```java
public final class BootStrapShipper {
    ShipperExecutor shipperExecutor;
    HandlerBuilder handlerBuilder;
    ShipperBuilder shipperBuilder;
    ShipperTaskBuilder shipperTaskBuilder;

    /**
     * 成员状态检查
     */
    private void stateCheck() {

        if (shipperBuilder == null) {
            if (handlerBuilder == null)
                handlerBuilder = new StandardHandlerBuilder();
            if (!handlerBuilder.initialized())
                handlerBuilder.reLoadHandler();
            shipperBuilder = new StandardShipperBuilder(handlerBuilder);
        }

        if (shipperTaskBuilder == null)
            shipperTaskBuilder = new StandardShipperTaskBuilder();
        if (shipperExecutor == null)
            shipperExecutor = new StandardShipperExecutor(ForkJoinPool.commonPool());
    }

    /**
     * @param dsl dsl
     * @return dsl中包含的多个任务
     */
    public List<CompletableFuture<ShipperTask>> submit(String dsl) {
        stateCheck();
        return shipperTaskBuilder
                .build(shipperBuilder.build(dsl))
                .stream()
                .map(shipperExecutor::submit)
                .collect(Collectors.toList());
    }


    /**
     * 如果 shipper 文件能读到,则执行这个 shipper
     *
     * @param sourceFile resources下的文件
     */
    public void executeBySource(String sourceFile) {
        Optional.ofNullable(Thread.currentThread().getContextClassLoader().getResource(sourceFile)).ifPresent(url -> {
            try {
                String dsl = new BufferedReader(new InputStreamReader(url.openStream()))
                        .lines()
                        .collect(Collectors.joining("\n"));
                List<CompletableFuture<ShipperTask>> submit = submit(dsl);
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(submit.toArray(new CompletableFuture[0]));
                voidCompletableFuture.whenComplete((v, e) -> voidCompletableFuture.completeExceptionally(e));
                voidCompletableFuture.join();
            } catch (IOException e) {
                log.error("", e);
            }
        });
    }
}
```

标准形式如下

1. 构造ShipperBuilder,并将string翻译为shipper
2. 构造ShipperTaskBuilder,并将shipper翻译为List\<ShipperTask>
3. 构造ShipperExecutor并将ShipperTask提交到其中,获得CompletableFuture\<ShipperTask>

### HandlerBuilder

HandlerBuilder实际上是插件的容器.

```java
public interface HandlerBuilder {
    /**
     * 加载Handler
     */
    void reLoadHandler();

    /**
     * @param name 处理器名字
     * @return 构造出的实例
     */
    Handler builderHandler(String name);

    /**
     *
     * @return 是否已经初始化
     */
    boolean initialized();
}
```

#### StandardHandlerBuilder

StandardHandlerBuilder是HandlerBuilder的标准实现

使用了类反射HandlerBuilder

参考了SPI的外部注入,其使用**META-INF/shipper.factories**

文件作为插件的包声明,这个文件中仅支持一个参数**handlerPackage**使用例子如下

```properties
handlerPackage=com.ponshine.shipper.plugin.implHandler
```

其中com.ponshine.shipper.plugin.implHandler即为插件的包名

### ShipperBuilder

如下

```java
/**
 * shipper的构造器
 */
public interface ShipperBuilder {
    Shipper build(String shipper);
}
```

#### Shipper

shipper实际上是一个java bean

```java
@Data
public class Shipper {
    String shipperDescribe;//shipper的描述
    Map<HandlerEnums, DSLDelegate> context;//描述构造的上下文
}
```

#### DSLDelegate

DSLDelegate是groovy,其中使用了groovy的元编程模型.具体可以参考groovy语言.

其作用是定义shipper语法

#### HandlerEnums

HandlerEnums是一个枚举类,定义了几种基本类型

```java
public enum HandlerEnums {
    INPUT("input"),
    FILTER("filter"),
    CODEC("codec"),
    OUTPUT("output")
    ;
    String name;

    HandlerEnums(String name) {
        this.name = name;
    }
}
```

#### StandardShipperBuilder

StandardShipperBuilder是ShipperBuilder的标准实现.

```java
public class StandardShipperBuilder implements ShipperBuilder {
    public static final String DEFAULT_BASE_SCRIPT = BaseShipperScript.class.getName();
    public static final String HANDLER_BUILDER_NAME = "handlerBuilder";
    public static final String HANDLER_EXECUTOR_NAME = "shipperTaskBuilder";
    public static final String HANDLER_MAP_NAME = "handlerMap";
    public static final String COMPLETABLE_FUTURE_NAME = "completableFuture";
    /**
     * dsl 的基础定义类名
     */
    private String baseScript = DEFAULT_BASE_SCRIPT;
    /**
     * 处理器build工厂
     */
    private HandlerBuilder standardHandlerBuilder;

    public StandardShipperBuilder(HandlerBuilder standardHandlerBuilder) {
        this.standardHandlerBuilder = standardHandlerBuilder;
    }

    @Override
    public Shipper build(String shipper) {
        Map<HandlerEnums, DSLDelegate> handlerMap = new HashMap<>();
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(baseScript);
        GroovyShell groovyShell = new GroovyShell(compilerConfiguration);
        groovyShell.setVariable(HANDLER_BUILDER_NAME, standardHandlerBuilder);
        groovyShell.setVariable(HANDLER_MAP_NAME, handlerMap);
        groovyShell.evaluate(shipper);
        Shipper result = new Shipper();
        result.setContext(handlerMap);
        result.setShipperDescribe(shipper);
        return result;
    }
}
```

可以看到,在build函数中,通过groovy将shipper字符串解释成了一个上下文.

### ShipperTaskBuilder

如下

```java
/**
 * 将shipper转化为ShipperTask
 *
 */
public interface ShipperTaskBuilder {
    List<ShipperTask> build(Shipper shipper);
}
```

#### ShipperTask

ShipperTask实现了Runnable接口,也就是说可以被提交到标准的线程池中.

```java
public interface ShipperTask extends Runnable,ShipperTaskContextAware,LogAware {
    /**
     * @return 当前异常列表
     */
    List<Exception> exceptions();

    /**
     * @return task 的状态
     */
    TaskStepEnum state();

    /**
     * 当前task中的event
     *
     * @return event list
     */
    Object nowEvents();

    default ShipperTask doing(){
        run();
        return this;
    }

}
```

ShipperTask有两个子接口,如下

```java
/**
 * 可调度的 ShipperTask
 */
public interface ScheduleShipperTask extends ShipperTask, Scheduled { }

/**
 * 循环的ShipperTask的实现
 */
public interface LoopShipperTask extends ShipperTask { }
```

作为两个标记接口,分别表示了task的两种特化类型

1. loop
2. cron

cron语法使用标准的quartz cron.

而基本的task类型则为simple.

通过在input域中描述一个cron.可以将simple类型的task当作cron类型来使用.

##### AbstractShipperTask

AbstractShipperTask提供了task的基本实现

基于这个基本实现,可以看到3种类型的task特化是非常简单的

###### StandardSimpleShipperTask

```java
@EqualsAndHashCode(callSuper = true)
@Data
public class StandardSimpleShipperTask extends AbstractShipperTask {
    @Override
    protected void doSomething() throws InterruptedException {
        doInput().doFilter().doOutPut();
    }
}
```

###### StandardScheduleShipperTask

```java
@EqualsAndHashCode(callSuper = true)
@Data
public class StandardScheduleShipperTask extends StandardSimpleShipperTask implements ScheduleShipperTask {
    private CronExpression cronExpression;
    @Delegate
    private Scheduled scheduled;

    @Override
    protected Input initInput(HandlerDefinition<Input> input) {
        Input handler = super.initInput(input);
        if (!(handler instanceof Scheduled))
            throw new ShipperException("input " + handler.getClass().getSimpleName() + " is not " + Scheduled.class.getSimpleName());
        scheduled = ((Scheduled) handler);
//        String cron = scheduled.cron();
//        try {
//            cronExpression = new CronExpression(cron);
//        } catch (ParseException e) {
//            throw new ShipperException(e);
//        }
        return handler;
    }

    @Override
    protected void doSomething() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted() && !scheduled.isInterrupted()) {
            if (trigger()) {
                long start = System.currentTimeMillis();
                super.doSomething();
                long end = System.currentTimeMillis();
                if (end - start < 1000)
                    TimeUnit.MILLISECONDS.sleep(1000 - (end - start));//至少等待一秒才进行下一次调度
            } else {
                TimeUnit.SECONDS.sleep(1);
            }
        }

    }
}
```

###### StandardLoopShipperTask

```java
@EqualsAndHashCode(callSuper = true)
@Data
public class StandardLoopShipperTask extends StandardSimpleShipperTask implements LoopShipperTask {

    private Recyclable recyclable;

    @Override
    protected Input initInput(HandlerDefinition<Input> input) {
        Input handler = super.initInput(input);
        if (!(handler instanceof Recyclable))
            throw new ShipperException("input " + handler.getClass().getSimpleName() + " is not " + Recyclable.class.getSimpleName());
        recyclable = (Recyclable) handler;
        return handler;
    }

    @Override
    protected void doSomething() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted() && !recyclable.isInterrupted() && recyclable.recyclable())
            super.doSomething();
    }

}
```

#### StandardShipperTaskBuilder

是ShipperTaskBuilder的标准实现

```java
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
```

这里提供了simple任务转化为cron任务的机制.

说明了两个限制,即一个处理过程必需存在一个输入和一个输出.

### Initialization

如果需要类进行初始化,则需要实现`Initialization`接口,如下

```java
/**
 * 初始化接口
 * 这个初始化接口实际上将在每次执行时调用
 * 因为filter和output实际上是可以运行时重载参数的(实际上input也可以)
 * 也就是说若需要实现"仅一次""的初始化,则需要在init函数中实现"仅一次"的语义
 */
public interface Initialization {
    void init();
}
```

### AutoCloseable

如果需要关闭资源,则需要实现AutoCloseable接口.

### 线程安全

shipper不保证线程安全性,虽然在AbstractShipperTask中有部分线程安全的说明,但实际上是为了安全发布而已.

### 生命周期

在标准实现中每个shipperTask中所有的插件都是一个独立的实例.但仅会反射一次.在task的执行过程中,对象一直不会被销毁.在执行完成后,才有可能在gc中被销毁.

## 开发一个插件

开发一个插件和编写一个类没有任何区别.

需要编写什么插件,就继承什么接口.唯一需要注意的是继承哪个接口,就实现该接口需要的方法,而不要去重写默认实现,除非这是想要的结果.

编写完了插件需要说明插件的用途以及插件配置的作用.时间充裕的情况下需要给插件编写大量的单元测试,并保证一定的代码覆盖率.

插件的名字不能相同.名字应该简单说明插件的用途.



