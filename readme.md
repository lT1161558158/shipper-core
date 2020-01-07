## shipper

### 简介
这是一个基于groovy的模仿了 `logstash` 的java实现.

数据在处理器之间以**事件**的形式流转,流转的方式类似于linux中的管道

```shell
cat randdata | awk '{print $2}' | sort | uniq -c
```

**输入**(就像cat命令)数据,然后处理**过滤**(就像awk或uniq)数据,最后**输出**(uniq)到其他地方

实际上的流转为

```shell
input|[codc]|[filter]|[codc]|[output]
```

shipper是用不同的**Handler**来实现这些的,默认情况下是使用**串行**来执行这些操作,但也可以通过**扩展**的方式来实现为广播事件并分配到不同的线程来执行,或者其他任意的方式.

shipper会给事件附加一些额外信息.例如**@timestamp**,**@version**等,一般来说,形如**@xx**格式的filed是组件附加的额外信息.

所有的**handler**都支持4个方法,方法会在**成功时执行**.(暂未实现)

- addTag
- removeTag
- addField
- removeField

### 配置方式

shipper的配置使用DSL,看起来像是一个gradle的配置文件

#### 区域

shipper使用`{}`定义区域,区域内可以包括区域定义,也可以定义键值对,如下

```dsl
input{
	stdin{
		
	}
	syslog{
		protocol udp
		port 514
	}
}
```

上述dsl定义了两个输入源,`stdin`和`syslog`,其中syslog采用的协议为`udp`,并从`514`端口获取数据

实际上input域中只有input的插件才会生效,其他域也类似

#### 外部域

#### codec
codec的配置比较特殊
它不属于基本域(input/filter/output)中的一部分而是在input或者output域中的一部分,但它也可以支持参数的设置,并且不设置参数的codec和设置参数的codec的编写方式截然不同
不配置参数的codec

```dsl
Stdin {
	codec "SimpleCodec"
}
Stdin {
	codec = "SimpleCodec"
}
```

配置参数的codec

```dsl
Stdin {
	codec {
		$"SimpleCodec"
		arg = "arg"
	}
}
Stdin {
	codec {
		$'SimpleCodec'
		arg "arg"
	}
}
```

配置参数的codec使用$来引用codec的名字.

#### 域名字

实际上类似于`StdIn`和`SimpleCodec`这样的名字就是类的名字.

这里也隐含了一个条件,编写的插件类名字不能够相同.

#### 数据类型

支持几乎所有的类型,甚至可以使用`new`关键字创建一个任意的对象,但一般情况下不这样做.

大部分情况下只需要简单的一些类型

- bool

  > debug true

- string

  > host "local1"

- number

  > port 514

- array

  > arr "123",456,true

- hash

  > hash [type:'web',p:123]

#### 引用

##### 事件引用

可以引用event中的内容,但只能访问到已经存在的内容.

event的处理过程可能如下

```shell
input|codc|filter1|filter2|codc|output
```

实际上由input产生的"事件",实际上应该称为一个匿名事件,它是一个原始的数据段,还没有为这个段绑定任何的名字,自然也无法在其他位置使用这个数据段.只有经过了至少一个`codec`后,这个数据段才是一个可以被访问的事件.

这里也隐含了一个特殊条件,那就是最简单的一个完整的`shipper`只需要一个`input`和一个`output`即可.输入数据段然后输出数据段.中间不进行任何操作.

```shell
input|output
```

实际上如果没有至少一个input和output的域,执行时会报告一个错误

> ```
> at least one output/input must be provided
> ```

引用事件内容的方式如下

```groovy
if (it['type'] == "web") {
    //xxx
}
```

**要注意的是,在input中无法访问`event`(实际上是在codec后才能访问),仅能在`filter`或者`output`中访问event,并且访问event的动作将先于`filter`或者`output`的初始化.也就是说可以按照event的情况来初始化filter或output**

##### 外部引用

在区域的内部引用外部域的内容,仅需要直接使用外部域的名字即可.

```java
p='ppp'
filter{
    hi  p
}
```

也可以使用

```java
p='ppp'
filter{
    hi  this[p]
}
```

不建议这么做,域中值的范围根据不同的实现可以是不同的

##### 内部引用

可以访问在当前域中定义的值

**要注意的是,只能向前引用(实际上event引用也遵循了这个原则),必须先说明是什么,然后才能使用**

#### 运算符

几乎支持所有的运算符,实际上是groovy

### 使用方式

#### 运行

主入口是`ShipperExecutor`接口

该接口是ExecutorService的子接口,通过代理的方式,所有的ExecutorService接口都由实际上的ExecutorService实现来执行,新增了以下两个接口

```java
default void execute(ShipperTask shipperTask) {
    submit(shipperTask);
}
CompletableFuture<ShipperTask> submit(ShipperTask shipperTask);
```

参数为ShipperTask

函数可能抛出`ShipperException`

一个标准的使用方式如下

```java
HandlerBuilder standardHandlerBuilder = new HandlerBuilder();
standardHandlerBuilder.reLoadHandler();
ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> {
    Thread thread = new Thread(r);
    thread.setUncaughtExceptionHandler((t, e) -> log.error("shipper executor error", e));
    return thread;
});
ShipperBuilder shipperBuilder=new StandardShipperBuilder(standardHandlerBuilder);
ShipperTaskBuilder shipperTaskBuilder = new StandardShipperTaskBuilder();
shipperTaskBuilder
                .build(shipperBuilder.build(dsl))
                .stream()
                .map(shipperExecutor::submit)
                .collect(Collectors.toList())
```

该接口实现了`AutoClosed`接口,会关闭相关资源

由上可知基本的使用方式如下

1. 构造`HandlerBuilder`
2. 加载(初始化)`HandlerBuilder`
3. 构造一个`ShipperTaskBuilder`
4. 构造一个`ShipperExecutor`
5. 执行对应的dsl

为了简单的使用提供了**BootstrapShipper**类,作为shipper的启动器

提供了两个方法以供使用

```java
List<CompletableFuture<ShipperTask>> submit(String dsl);
void executeBySource(String sourceFile);
```



#### 持续运行

实际上input决定了一个shipperTask是否持续执行.在标准实现中input具有3种类型,simple,loop和cron.他们各自的意义也显而易见

需要注意的是simple的input可以简单的变为cron类型

只需要在input域中添加cron字段即可.如下

```shipper
input {
    IdentityInput {
        identity = "1234"
    }
    cron = "*/5 * * * * ?"
}
output {
    StdOutput {}
}
```



每个input都对应了一个执行线程,后续的filter之类的操作实际上是在这个线程中由上往下执行的.如果需要更改这个行为,可以重新实现task和builder.

若想在运行时中止执行,可以通过在dsl中描述一个中止异常来进行.例如

```dsl
if(it['message']=="end")
        throw new Exception("stop")
```

也可以通过input访问中止策略.即close函数.也可以使用中断中止任务运行.

### 插件

插件分为4类

1. input
2. output
3. filter
4. codec

其中对于3,4最好的实现是纯代码,但有时需要保存一些状态,则需要考虑到实例状态.

一个插件在一个dsl中只会进行生成一次,但对每个event应用插件动作时,会重新执行一次dsl中的初始化过程.

也就是说在同一个shipper任务的执行过程中,插件是单例的,但插件每次使用都会重新初始化成员.



