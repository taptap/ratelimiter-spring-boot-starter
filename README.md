# ratelimiter-spring-boot-starter

基于 redis 的偏业务应用的分布式限流组件，目前支持`时间窗口`、`令牌桶`
两种限流算法。使得项目拥有分布式限流能力变得很简单。限流的场景有很多，常说的限流一般指网关限流，控制好洪峰流量，以免打垮后方应用。这里突出`偏业务应用的分布式限流`
的原因，是因为区别于网关限流，业务侧限流可以轻松根据业务性质做到细粒度的流量控制。比如如下场景，

- 案例一：有一个公开的 openApi 接口， openApi 会给接入方派发一个 appId，此时，如果需要根据各个接入方的 appId 限流，网关限流就不好做了，只能在业务侧实现

- 案例二：公司内部的短信接口，内部对接了多个第三方的短信通道，每个短信通道对流量的控制都不尽相同，假设有的第三方根据手机号和短信模板组合限流，网关限流就更不好做了

以上举例的场景，通过 ratelimiter-spring-boot-starter 可以轻松解决限流问题

## 限流算法说明

- `时间窗口限流`：偏向控制请求数量，比如每秒请求数量不超过 100，每分钟请求数量不超过 1000，每小时请求数量不超过 10000，每天请求数量不超过 100000。
- `令牌桶限流`：偏向控制请求频率，比如最大请求并发不超过 100，且 QPS 限制在一定范围内，比如 QPS 限制在 50。

关于限流算法更详细的信息：http://www.kailing.pub/article/index/arcid/251.html

## 1、快速开始

### 1.1、添加组件依赖，已上传到maven中央仓库

maven

```xml

<dependency>
  <groupId>com.github.taptap</groupId>
  <artifactId>ratelimiter-spring-boot-starter</artifactId>
  <version>1.3</version>
</dependency>

```

gradle

```groovy
implementation 'com.github.taptap:ratelimiter-spring-boot-starter:1.3'
```

### 1.2、application.properties 配置

```properties
spring.ratelimiter.enabled=true
spring.ratelimiter.redis-address=redis://127.0.0.1:6379
spring.ratelimiter.redis-password=xxx
```

启用 ratelimiter 的配置必须加，默认不会加载。

### 1.3、在需要加限流逻辑的方法上，添加注解 @RateLimit，如：

```java

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s")
    public String get(String name) {
        return "hello";
    }
}
```

#### 1.3.1 @RateLimit 注解说明

|属性               | 单位 | 默认值 |是否必填|描述|
| :----            | :---- | :---- |:---- |:---- |
| mode             | enum（TIME_WINDOW/TOKEN_BUCKET） | TIME_WINDOW |否|限流模式,目前可选时间窗口和令牌桶|
| rate             | int     | 无 |是|时间窗口模式表示每个时间窗口内的请求数量、令牌桶模式表示每秒的令牌生产数量|
| rateInterval     | String | 1s |否|用于时间窗口模式，表示时间窗口|
| rateExpression   | String | 无 |否|通过 EL 表达式从 Spring Config 上下文中获取 `rate` 的值，`rateExpression` 的优先级比 `rate` 高|
| fallbackFunction | String | 无 |否|自定义触发限流时的降级策略方法，默认触发限流会抛 `RateLimitException` 异常|
| customKeyFunction | String | 无 |否|自定义获取限流 `key` 的方法|
| bucketCapacity    | int | 无 |否|用于令牌桶模式，表示令牌桶的桶的大小，这个参数控制了请求最大并发数|
| bucketCapacityExpression | String | 无 |否|通过 EL 表达式从 Spring Config 上下文中获取 `bucketCapacity` 的值，`bucketCapacityExpression` 的优先级比 `bucketCapacity` 高|
| requestedTokens   | int | 1 |否|用于令牌桶模式，表示每次获取的令牌数，一般不用改动这个参数值，除非你知道你在干嘛|

@RateLimit 注解可以添加到任意被 spring 管理的 bean 上，不局限于 controller ，service 、repository 也可以。在最基础限流功能使用上，以上三个步骤就已经完成了。

#### 1.3.2 限流的粒度，限流 key

限流的粒度是通过限流的 key 来做的，在最基础的设置下，限流的 key 默认是通过方法名称拼出来的，规则如下：

```properties
key=RateLimiter_ + 类名 + 方法名
```

除了默认的 key 策略，ratelimiter-spring-boot-starter 充分考虑了业务限流时的复杂性，提供了多种方式。结合业务特征，达到更细粒度的限流控制。

#### 1.3.3 触发限流后的行为

默认触发限流后 程序会返回一个 http 状态码为 429 的响应，响应值如下：

```json
{
  "code": 429,
  "msg": "Too Many Requests"
}
```

同时，响应的 header 里会携带一个 Retry-After 的时间值，单位 s，用来告诉调用方多久后可以重试。当然这一切都是可以自定义的，进阶用法可以继续往下看

## 2、进阶用法

### 2.1、自定义限流的 key

自定义限流 key 有三种方式，当自定义限流的 key 生效时，限流的 key 就变成了（默认的 key + 自定义的 key）。下面依次给出示例

#### 2.1.1、@RateLimitKey 的方式

```java

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s")
    public String get(@RateLimitKey String name) {
        return "get";
    }
}
```

@RateLimitKey 注解可以放在方法的入参上，要求入参是基础数据类型，上面的例子，如果 name = kl。那么最终限流的 key 如下：

```properties
key=RateLimiter_com.taptap.ratelimiter.web.TestController.get-kl
```

#### 2.1.2、指定 keys 的方式

```java

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s", keys = {"#name"})
    public String get(String name) {
        return "get";
    }

    @GetMapping("/hello")
    @RateLimit(rate = 5, rateInterval = "10s", keys = {"#user.name", "user.id"})
    public String hello(User user) {
        return "hello";
    }
}
```

keys 这个参数比 @RateLimitKey 注解更智能，基本可以包含 @RateLimitKey 的能力，只是简单场景下，使用起来没有 @RateLimitKey 那么便捷。keys 的语法来自 spring 的 `Spel`
，可以获取对象入参里的属性，支持获取多个，最后会拼接起来。使用过 spring-cache 的同学可能会更加熟悉 如果不清楚 `Spel` 的用法，可以参考 spring-cache 的注解文档

#### 2.1.3、自定义 key 获取函数

```java

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s", customKeyFunction = "keyFunction")
    public String get(String name) {
        return "get";
    }

    public String keyFunction(String name) {
        return "keyFunction" + name;
    }
}
```

当 @RateLimitKey 和 keys 参数都没法满足时，比如入参的值是一个加密的值，需要解密后根据相关明文内容限流。可以通过在同一类里自定义获取 key 的函数，这个函数要求和被限流的方法入参一致，返回值为 String
类型。返回值不能为空，为空时，会回退到默认的 key 获取策略。

### 2.2、自定义限流后的行为

#### 2.2.1、配置响应内容

```properties
spring.ratelimiter.enabled=true
spring.ratelimiter.response-body=Too Many Requests
spring.ratelimiter.status-code=509
```

添加如上配置后，触发限流时，http 的状态码就变成了 509 。响应的内容变成了 Too Many Requests 了

#### 2.2.2、自定义限流触发异常处理器

默认的触发限流后，限流器会抛出一个异常，限流器框架内定义了一个异常处理器来处理。自定义限流触发处理器，需要先禁用系统默认的限流触发处理器，禁用方式如下：

```properties
spring.ratelimiter.exceptionHandler.enable=false
```

然后在项目里添加自定义处理器，如下：

```java

@ControllerAdvice
public class RateLimitExceptionHandler {

    private final RateLimiterProperties limiterProperties;

    public RateLimitExceptionHandler(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

    @ExceptionHandler(value = RateLimitException.class)
    @ResponseBody
    public String exceptionHandler(HttpServletResponse response, RateLimitException e) {
        response.setStatus(limiterProperties.getStatusCode());
        response.setHeader("Retry-After", String.valueOf(e.getRetryAfter()));
        return limiterProperties.getResponseBody();
    }
}
```

#### 2.2.3、自定义触发限流处理函数，限流降级

```java

@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s", fallbackFunction = "getFallback")
    public String get(String name) {
        return "get";
    }

    public String getFallback(String name) {
        return "Too Many Requests" + name;
    }

}
```

这种方式实现和使用和 2.1.3、自定义 key 获取函数类似。但是多一个要求，返回值的类型需要和原限流函数的返回值类型一致，当触发限流时，框架会调用 fallbackFunction 配置的函数执行并返回，达到限流降级的效果

### 2.3、 动态设置限流大小

#### 2.3.1、rateExpression 的使用

从 `v1.2` 版本开始，在 `@RateLimit` 注解里新增了属性 rateExpression。该属性支持 `Spel` 表达式从 Spring 的配置上下文中获取值。 当配置了 rateExpression 后，rate
属性的配置就不生效了。使用方式如下：

```java
    @GetMapping("/get2")
@RateLimit(rate = 2, rateInterval = "10s", rateExpression = "${spring.ratelimiter.max}")
public String get2(){
        return"get";
        }
```

集成 apollo 等配置中心后，可以做到限流大小的动态调整在线热更。

### 2.4、直接使用限流器服务-`RateLimiterService`
从 `v1.3` 版本开始，限流器框架内部提供了一个限流器服务，可以直接使用。当使用 `RateLimiterService` 后，则不用关心`限流注解`的逻辑了，所有限流逻辑都可以高度定制，如下：
```java
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RateLimiterService limiterService;

    @GetMapping("/limiterService/time-window")
    public String limiterServiceTimeWindow(String key) {
        Rule rule = new Rule(Mode.TIME_WINDOW); // 限流策略,设置为时间窗口
        rule.setKey(key); //限流的 key
        rule.setRate(5); //限流的速率
        rule.setRateInterval(10); //时间窗口大小，单位为秒
        Result result = limiterService.isAllowed(rule);
        if (result.isAllow()) { //如果允许访问
            return "ok";
        } else {
            //触发限流
            return "no";
        }
    }

    @GetMapping("/limiterService/token-bucket")
    public String limiterServiceTokenBucket(String key) {
        Rule rule = new Rule(Mode.TOKEN_BUCKET); // 限流策略,设置为令牌桶
        rule.setKey(key); //限流的 key
        rule.setRate(5); //每秒产生的令牌数
        rule.setBucketCapacity(10); //令牌桶容量
        rule.setRequestedTokens(1); //请求的令牌数
        Result result = limiterService.isAllowed(rule);
        if (result.isAllow()) { //如果允许访问
            return "ok";
        } else {
            //触发限流
            return "no";
        }
    }
}
```

## 3、集成示例、测验

### 3.1、集成测验

启动 src/test/java/com/taptap/ratelimiter/Application.java 后，访问 http://localhost:8080/swagger-ui.html

### 3.2、压力测试

- 压测工具 wrk： https://github.com/wg/wrk
- 测试环境: 8 核心 cpu ，jvm 内存给的 -Xms2048m -Xmx2048m ，链接的本地的 redis

```shell
#压测数据
kldeMacBook-Pro-6:ratelimiter-spring-boot-starter kl$ wrk -t16 -c100 -d15s --latency http://localhost:8080/test/wrk
Running 15s test @ http://localhost:8080/test/wrk
  16 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     6.18ms   20.70ms 281.21ms   98.17%
    Req/Sec     1.65k   307.06     2.30k    76.44%
  Latency Distribution
     50%    3.57ms
     75%    4.11ms
     90%    5.01ms
     99%  115.48ms
  389399 requests in 15.03s, 43.15MB read
Requests/sec:  25915.91
Transfer/sec:      2.87MB
```

压测下，所有流量都过限流器，qps 可以达到 2w+。

## 4、版本更新

### 4.1、（v1.1.1）版本更新内容

- 1、触发限流时，header 的 Retry-After 值，单位由 ms ，调整成了 s

### 4.2、（v1.2）版本更新内容

- 1、触发限流时，响应的类型从 `text/plain` 变成了 `application/json`
- 2、优化了限流的 lua 脚本，将原来的两步 lua 脚本请求，合并成了一个，减少了和 redis 的交互
- 3、限流的时间窗口大小，支持 `Spel` 从 Spring 的配置上下文中获取，结合 `apollo` 等配置中心后，支持规则的动态下发热更新

### 4.3、（v1.3）版本更新内容

- 1、配置策略变化，不在从应用的上下文中获取 Redis 数据源，而是必须配置。但是配置的数据源在 Spring 上下文中声明了 `rateLimiterRedissonBeanName`，应用也可以获取使用
- 2、代码重构，新增了`令牌桶`的限流策略支持
- 3、抽象了限流器服务 `RateLimiterService`，并在 Spring 上下文中声明了，应用可以直接注入使用
