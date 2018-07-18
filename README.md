Theta Framework
---

## 简介
===

Theta Framework是基于Spring Cloud整体架构而开发出的一系列通用工具和框架增强方案的集合程序包，目前已经开始逐步实现的方向包括：

    * 核心类及通用语言类(core)
    * 微服务架构增强类(cloud)
    * 配置类(config)
    * 安全类(security)

## 核心类及通用语言类
===

### 接口编程规范

给出了基于接口编程规范所需的基础支撑类，包括:

* `Request` : 入参的实体类，所有的入参都需要继承该类，从而使得接口入参只有唯一一个参数。同时需要定义序列号编码。
* `Result` : 返回值实体类，所有的入参都需要继承该类，并定义序列号编码。其中默认参数包含调用是否成功以及相关错误码和错误信息。
* `AppException` : 业务异常封装类，所有接口以下的业务异常抛出需要经过`AppException`的封装，以便于上层统一处理，同时防止程序的异常中断。
* `ErrorCode` : 公用的异常编码定义类，特定的业务系统需要特定的异常编码时，可以在各自工程定义。

据此，一个默认的接口定义形式应该如下：

<pre name="code" class="java">
    <code>
    public interface SampleService{
        public SampleResult executeMethod(SampleRequest request)
    }
    </code>
</pre>

> 其中`SampleRequest`和`SampleResult`分别继承与Theta Framework中的`Request`和`Result`基类。

### ServiceTrace

ServiceTrace框架主要功能包括两个方面:
    
1. 基于接口层面(采用Spring AOP方式)的方法执行跟踪，并在日志中打印具体的执行方法、执行时间等参数。
2. 业务接口的最高层异常捕获及处理，防止业务代码异常继续上抛从而影响整体应用的稳定性(或远程调用时的错误传递)。

ServiceTrace的使用方法非常简单，只需两步：

1. 在SpringBoot的启动类的类描述注解中添加`@ServiceTraceApplication`注解，如有需要，在配置文件中添加相关配置项:
    <pre name="code" class="yaml">
        <code>
        theta:
            service-trace:
                threshold: 3000 
                #执行时间临界值，单位毫秒，默认值为1000。不会影响方法的实际执行，如果方法执行超过该时间，则会在输出日志的BT(Beyond threshold)项中输出Y(yes)
                filterParams：
                    - indexService
                    - **Controller
                # 过滤参数，按照类名对ServiceTrace生效的范围进行控制，输入类名则不对该类进行跟踪，前置**通配符则按照统配规则进行过滤
        </code>
    </pre>
2. 在需要进行ServiceTrace的**接口实现类**的类描述注解上添加`@ServiceTrace`注解，该实现类**必须为Spring所管控的Bean实例**才可以使其起作用，如果为手工创建类，则无法被ServiceTrace框架追踪到。

## 微服务架构增强类
===

基于Spring Cloud的微服务解决方案，我们无需另外再去研发一套不同的实现，目前我们所做的是基于该方案，在沿用其自身的restful api调用方式的同时，提供一套封装好的远程代理类，使得我们的整个微服务系统的系统内方法调用可以做到像本地调用一样便捷。

>当然，本身也会对编码规范有一些限制，如开放服务的接口实现类只能在Spring上下文中存在一个Bean等，不过在编写对外服务这一层面，一般不会存在过于复杂的Bean配置需求，如果有，也可以在下层屏蔽。

### Producer端增强

基本原理是定义一个Controller用以提供resultfu api服务，但是与普通的Spring Cloud不同的是，这个Controller根据配置项中的信息，直接将本地的接口实现按照一定的命名规则开放出去，不再需要开发者逐个编写。

使用方法：

1. SpringBoot的启动类类描述注解中添加`@EnableCloudProducer`注解。
2. 配置Producer相关信息：
    <pre name="code" class="yaml">
        <code>
        cloud:
            producer:
                producerClasses:
                    - org.theta.cloud.eureka.client.ext.HelloExtService
                    # 需对外开放的接口全称列表，需要确保这些对外接口只有一个Bean实例
        </code>
    </pre>

### Consumer端增强

基本原理是在本地Spring Bean加载之前，生成远程类的代理类，包装了远程调用方法(目前有基于ribbon的LoadBalance)，然后就可以像使用本地Bean一样使用外部服务了。

1. SpringBoot的启动类类描述注解中添加`@EnableCloudConsumer`注解。
2. 配置Consumer相关信息：
    <pre name="code" class="yaml">
        <code>
            theta:
                cloud:
                    consumer:
                        consumerClasses:
                            producer: #Eureka中的服务名称
                            - org.theta.cloud.eureka.client.ext.HelloExtService #需要消费的接口类全称
        </code>
    </pre>
3. 在本地的Bean中使用`@Autowired`或`@Resource`注解像本地类一样加载远程类。

## 配置类
===

### Kafka Log Appender配置

目前我们将Logback Kafka的源码置于包中，日后转为framework方案时会剔除该部分，并使用实时版本。所以此处不再展开，如果有使用需要，只需查看样例工程中的logback-spring.xml配置即可，相关的配置类已经在包中给出。

> 目前开源包内暂时不集成Logback Kafka配置。

## 安全类
===

### SpringBoot配置加解密

目前我们使用jasypt的解决方案(外部包)，同时加解密算法使用同一的SM4(公司内部实现),只需要在SpringBoot的启动类类描述注解中添加`@EnableJasyptForSM4`注解即可，然后在配置文件需要加密的配置项中使用加密后的配置并用`ENC()`包裹即可。

> 目前开源包内暂时不集成SM4算法，需要使用直接接入Jasypt即可。

@author Ranger Chen