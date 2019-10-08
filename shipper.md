## oh-my-shipper

### 简介
这是一个基于groovy的模仿了 `logstash` 的java实现.

数据在处理器之间以**事件**的形式流转,流转的方式类似于linux中的管道

```shell
cat randdata | awk '{print $2}' | sort | uniq -c
```

**输入**(就像cat命令)数据,然后处理**过滤**(就像awk或uniq)数据,最后**输出**(uniq)到其他地方

实际上的流转为

```shell
input|codc|filter|codc|output
```

shipper是用不同的**Handler**来实现这些的,默认情况下是使用**串行**来执行这些操作,但也可以通过**扩展**的方式来实现为广播事件并分配到不同的线程来执行,或者其他任意的方式.

shipper会给事件附加一些额外信息.例如**@timestamp**,**@version**等,一般来说,形如**@xx**格式的filed是组件附加的额外信息.如果试图自行添加一个**@xx**格式的filed,shipper会报告一个错误,并且会根据执行器的不同来决定是否忽略这个非法的filed或者直接拒绝执行这个filed.一般来说是直接拒绝执行,因为这不符合预期.

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

#### 外部域

在{}外部可以直接定义一些值,但这些定义的值一般仅作为被引用的内容

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

  > hash type:'web',p:123

#### 引用

##### 事件引用

可以引用event中的内容,但只能访问到已经存在的内容.

event的处理过程可能如下

```shell
input|codc|filter1|filter2|codc|output
```

实际上由input产生的"事件",实际上应该称为一个匿名事件,它是一个原始的数据段,还没有为这个段绑定任何的名字,自然也无法在其他位置使用这个数据段.只有经过了至少一个`codc`后,这个数据段才是一个可以被访问的事件.

这里也隐含了一个特殊条件,那就是最简单的一个完整的`shipper`只需要一个`input`和一个`output`即可.输入数据段然后输出数据段.中间不进行任何操作.

```shell
input|output
```

实际上仅仅只有output也是可以作为一个`shipper`的,但大部分情况这种操作下是没有意义的(也许debug的时候可以使用)

引用事件内容的方式如下

```groovy
if (it['type'] == "web") {
    grok {
        match {
            message "\\s+(?<request_time>\\d+(?:\\.\\d+)?)\\s+"
        }
    }
}
```

**要注意的是,在input中无法访问`event`(实际上是在codec后才能访问),仅能在`filter`或者`output`中访问event,并且访问event的动作将先于`filter`或者`output`的执行.**

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

##### 内部引用

可以访问在其他域中定义的值

**要注意的是,只能向前引用(实际上event引用也遵循了这个原则)**

#### 运算符

几乎支持所有的运算符,实际上是groovy