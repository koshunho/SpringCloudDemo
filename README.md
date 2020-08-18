# SpringCloudDemo

* [SpringCloudDemo](#springclouddemo)
	* [五大组件](#五大组件)
	* [Eureka服务注册、发现](#eureka服务注册-发现)
		* [springcloud-eureka-7001](#springcloud-eureka-7001)
		* [springcloud-provider-dept-8001](#springcloud-provider-dept-8001)
	* [Eureka自我保护机制](#eureka自我保护机制)
	* [Ribbon 负载均衡](#ribbon-负载均衡)
		* [springboot-consumer-dept-81](#springboot-consumer-dept-81)
		* [详解@LoadBalanced](#详解loadbalanced)
		* [RestTemplate的IRule](#resttemplate的irule)
		* [Ribbon核心组件IRule](#ribbon核心组件irule)
	* [Feign](#feign)
	* [Hystrix](#hystrix)
		* [服务熔断](#服务熔断)
		* [服务降级](#服务降级)
		* [熔断 和 降级 差异](#熔断-和-降级-差异)
		* [服务监控](#服务监控)
	* [Zuul 路由网关](#zuul-路由网关)

---
1. 导入依赖
2. 编写配置文件
3. 开启这个功能 `@EnableXXXXX`
4. 配置类

---

### 五大组件
服务发现—— Eureka

负载均衡——Ribbon

断路器——Hystrix

服务网关——Zuul

分布式配置——Spring Cloud Config

---
### Eureka服务注册、发现
Eureka Server提供服务注册服务，各个节点启动后，会在Eureka Server中进行注册，这样Eureka Server中的服务注册表中会存储所有可用服务节点的信息。

Eureka Client是一个Java客户端，用于简化Eureka Server是交互，客户端同时也具备一个内置的使用**轮询负载**算法的负债均衡器。在应用启动后，将会向Eureka Server发送心跳（默认周期为30s）。如果Eureka Server 在多个心跳周期内没有接收到某个节点的心跳，Eureka Server将会从服务注册表中把这个服务节点移除掉（默认周期为90s）

![监控页面](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597644013917.png)

特别注意**服务名**，Consumer通过RestTemplate访问Provider的时候，就通过服务名来访问，而不是URL！
```java
    // private static final String REST_URL_PREFIX = "http://localhost:8001";
    // Ribbon。我们这里的地址应该是一个变量，通过服务名来访问
    private static final String REST_URL_PREFIX = "http://SPRINGCLOUD-PROVIDER-DEPT";
```

![服务名](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597594499810.png)

##### springcloud-eureka-7001

1. 依赖
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka-server</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
```

2. applicaiton.yaml
```yaml
server:
  port: 7001

eureka:
  instance:
    hostname: localhost # Eureka服务端的实例名称
  client:
    register-with-eureka: false # 表示是否向Eureka注册中心注册自己
    fetch-registry: false # 如果为false 表示自己为注册中心
    service-url: # 监控页面
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/ #设置于Eureka Server交互的地址查询服务和注册服务都需要依赖这个defaultZone地址
```

3. 主启动类
   注意`@EnableEurekaServer`！
```java
@SpringBootApplication
@EnableEurekaServer //EnableEurekaServer 声明这是一个服务端的启动类，可以接受别人注册进来
public class EurekaServer_7001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServer_7001.class, args);
    }
}
````

##### springcloud-provider-dept-8001
把8001的服务注册到7001的eureka中

1. 依赖
```xml
        <!--加入eureka的依赖-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
        <!--actuator：完成监控信息-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!--我们需要拿到实体类，所以要配置api module-->
        <dependency>
            <groupId>com.huang</groupId>
            <artifactId>springcloud-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
		...
```

2. applicaiton.yaml
- 特别注意`spring.applicaiton.name`这个属性！后面负载均衡 多个instance的 服务名称 都必须是这个！
```yaml
server:
  port: 8001

mybatis:
  type-aliases-package: com.huang.springcloud.pojo
  mapper-locations: classpath:mybatis/mapper/*.xml
  config-location: classpath:mybatis/mybatis-config.xml

spring:
  application:
    name: springcloud-provider-dept # 3个服务名称必须一致
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db01?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456

# Eureka的配置，服务注册到哪里
eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/  #也就是7001的注册中心的地址！
  instance:
    instance-id: springcloud-provider-dept8001 #修改eureka默认的描述信息

#info配置
info:
  app.name: koshunho-springcloud
  company.name: Waseda Daigaku

```

3. 主启动类
   注意`@EnableEurekaClient`！在服务启动后自动注册到Eureka中！
```java
@SpringBootApplication
@EnableEurekaClient //在服务启动后自动注册到Eureka中
@EnableDiscoveryClient //服务发现
public class DeptProvider_8001 {
    public static void main(String[] args) {
        SpringApplication.run(DeptProvider_8001.class, args);
    }
}
````
---

### Eureka自我保护机制
把一个Client断开

![爆红](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597594480423.png)

某个时刻某个微服务不能使用了，Eureka不会立刻清理，依旧会对该微服务的信息进行保存。

默认情况下，如果Eureka Server在一定时间内没有接收到某个微服务实例的心跳，Eureka Server将会注销该实例（默认90s）。但是当**网络分区故障**发生时，微服务与Eureka无法正常通信，但是此时微服务本身是健康的，**此时本不应该注销这个服务**。Eureka通过自我保护机制来解决这个问题，当Eureka Server节点在短时间内丢失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。

一旦进入该模式，Eureka Server就会保护服务注册表中的信息，不再删除服务注册表中数据（也就是不会注销任何微服务）。该网络故障恢复后（心跳数重新恢复到阈值以上时），该Eureka Server节点就会自动退出自我保护模式。

--> 宁可保留错误的服务注册信息，也不盲目注销任何可能健康的服务实例。
-->好死不如赖活着

自我保护模式是一种**应对网络异常**的安全保护措施。

在SpringCloud中，使用`eureka.server.enable-self-preservation = false`禁用自我保护模式（不推荐关闭）。

---
### Ribbon 负载均衡

负载均衡简单分类：
- 集中式LB
   + Nginx
   + 在服务的消费方和提供方之间提供独立的LB设施，由该设施负责把访问请求通过某种策略转发至服务的提供方
- 进程式LB
  + Ribbon
  + 将LB逻辑继承到消费方，**消费方从服务注册中心获取有哪些地址可用**，然后自己再从这些地址中选出一个合适的服务器。Ribbon就是集成于消费方进程，**消费方通过它来获取到服务提供方的地址**！

##### springboot-consumer-dept-81
1. 依赖
```xml
        <!--Ribbon-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-ribbon</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
```

2. application.yaml
   - 追加Eureka的服务注册地址！
```yaml
server:
  port: 81

eureka:
  client:
    register-with-eureka: false #不向eureka注册自己（因为这是客户端）
    service-url:
      defaultZone: http://localhost:7001/eureka/
```

3. 在getRestTemplate()方法加上`@LoadBalanced`，在获得RestTemplate时加入Ribbon的配置
```java
@Configuration
public class MyConfig {

    @Bean
    @LoadBalanced //Ribbon
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```

4. 主启动类加上`@EnableEurekaClient`
```java
// Ribbon 和 Eureka 整合以后，客户端可以直接调用，不用关心IP和端口号，也就是可以直接通过服务名来访问provider
@SpringBootApplication
public class DeptConsumer_81 {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer_81.class, args);
    }
}
```

5. 根据服务名访问Provider! 可以直接调用服务而不用再关心地址和端口号
```java
    @Autowired
    private RestTemplate restTemplate;

    // private static final String REST_URL_PREFIX = "http://localhost:8001";
    // Ribbon。我们这里的地址应该是一个变量，通过服务名来访问
    private static final String REST_URL_PREFIX = "http://SPRINGCLOUD-PROVIDER-DEPT";

    @RequestMapping("/consumer/dept/get/{id}")
    public Dept get(@PathVariable("id") Long id){
        return restTemplate.getForObject(REST_URL_PREFIX + "/dept/get/" + id, Dept.class);
    }
```

Ribbon默认采用轮训的策略，来配置3个Provider来看看效果。 特别注意，对外必须暴露统一的服务实例名！
```yaml
spring:
  application:
    name: springcloud-provider-dept # 3个服务名称必须一致
```

![](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597594537611.png)

![](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597594557988.png)

![](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597594569321.png)

##### 详解@LoadBalanced

[详解](https://blog.csdn.net/xiao_jun_0820/article/details/78917215)

有没有觉得很奇怪，为什么RestTemplate上面@LoadBalanced注解，就能实现负载均衡

![@LoadBalanced](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597594628343.png)

点进去这个注解的源码，发现也没有什么特别的地方。（**注意@Qualifier**）

![@LoadBalanced](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597649165060.png)

搜索`@LoadBalanced`注解的使用地方，发现只有一处使用了,在`LoadBalancerAutoConfiguration`这个自动装配类中。

![LoadBalancerAutoConfiguration](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597597535072.png)

![LoadBalancerAutoConfiguration](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597650138163.png)

可以看到就是利用了RestTempllate的拦截器，使用RestTemplateCustomizer对所有标注了`@LoadBalanced`的RestTemplate Bean添加了一个LoadBalancerInterceptor拦截器，而这个拦截器的作用就是对请求的URI进行转换获取到具体应该请求哪个服务实例ServiceInstance。

那么为什么
```java
@LoadBalanced
@Autowired(required = false)
private List<RestTemplate> restTemplates = Collections.emptyList();
```
能够将所有标注了`@LoadBalanced`的RestTemplate自动注入进来呢？

还有关键的一点是：需要注入拦截器的目标restTemplates到底是哪一些？因为RestTemplate实例在context中可能存在多个，不可能所有的都注入拦截器，这里就是`@LoadBalanced`注解发挥作用的时候了。

大家日常使用很多都是用@Autowired来注入一个bean,其实@Autowired还可以注入List和Map。

**@LoadBalanced中包含了@Qualifier，只有加上了这个注解的才会被注入到List和Map中**。所以当Spring容器中有多个相同类型的bean的时候，可以通过@Qualifier来进行区分，以便在注入的时候明确表明你要注入具体的哪个bean，消除歧义。


##### RestTemplate的IRule

Robbin默认采用轮训的方式，如果我们想更改呢？
[详解](https://zhuanlan.zhihu.com/p/114932698)

答案同样在`LoadBalancerAutoConfiguration`的`LoadBalancerInterceptor`中

对于Ribbon，对应的`LoadBalancerInterceptor`就是`RibbonLoadBalancerClient`

![RibbonLoadBalancerClient](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597650609386.png)

重点看getServer方法如何选择服务！

![getServer()](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597650677520.png)

发现是`ILoadBalancer`接口的方法

![ILoadBalancer](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597650946237.png)

由于它是接口，我们点进去看他的一个实现类是怎么实现这个方法的。

在`BaseLoadBalancer`中，这个类继承了`AbstractLoadBalancer`，而``AbstractLoadBalancer`` 实现了 ``ILoadBalancer``

![chooseServer](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597651167820.png)

我们发现这个rule，实际上是一个`IRule`

![IRule](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597651219192.png)

--->终于可以得出结论

要想切换策略，我们就可以在一个配置类中，向容器添加一个``IRule``的策略！

```java
@Configuration
public class MyConfig {
    @Bean
    public IRule myRule(){
	    //使用随机算法来替代默认的轮训
        return new RandomRule();
    }
}
```
当然也可以自定义自己的负载均衡算法，这个后面有时间再看看吧

##### Ribbon核心组件IRule
[各种策略](https://www.cnblogs.com/LQBlog/p/10084581.html)

---
### Feign
Feign是声明式的Web Service客户端，让微服务之间的调用变得更简单了，类似Controller调用Service。

Feign集成了Ribbon！利用Ribbon维护了 某一个服务名 下的instance的信息，并且通过轮询的方式实现了客户端的负载均衡。而与Ribbon不同的是，只需要通过Feign只需要定义服务绑定接口，相比之下更优雅简单。

之前是Ribbon + RestTemplate。Feign感觉就是封装了RestTemplate，在Feign的实现下，我们只需要创建一个接口并使用注解的方式来配置它（类似以前Dao接口上标注`@Mapper`，现在是微服务接口上标注`@FeignClient`即可）。

1. 依赖
```xml
        <!--feign-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
```

2. 写一个接口DeptClientService
   - 需要绑定服务名！`@FeignClient(value = "SPRINGCLOUD-PROVIDER-DEPT")`！Feign就绑定了Provider的服务名，Consumer就通过接口来调用Feign提供的父服务
```java
@FeignClient(value = "SPRINGCLOUD-PROVIDER-DEPT")
public interface DeptClientService {

    @RequestMapping("/dept/get/{id}")
    Dept queryById(@PathVariable("id") Long id);

    @RequestMapping("/dept/list")
    List<Dept> queryAll();

    @RequestMapping("/dept/add")
    boolean addDept(Dept dept);
}

```
3. 修改Controller
![Feign Controller](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597741343822.png)

4. 主启动类
   - 加上 `@EnableFeignClients` 就能扫描到springcloud-api写的DeptClientServiceLe


P.S. Feign自带负载均衡！要是我想修改负载均衡策略咋办？

还是和Robbin差不多，只是**不需要**在getRestTemplate()方法上面加上`@LoadBalanced`了！！

```java
@Configuration
public class MyConfig {
    @Bean
    public IRule myRule(){
	    //使用随机算法来替代默认的轮训
        return new RandomRule();
    }
}
```

---
### Hystrix

分布式体系结构中各个服务会有很多依赖关系，每个依赖关系在某些时候会出现失败，从而导致服务血崩。

扇出： 多个微服务之间调用时，假设微服务A 调用 微服务B和C，微服务B 和 微服务C 又调用其他的微服务。

如果扇出的链路上某个微服务的调用响应时间过长 or 不可用，对微服务A 的调用就会占用越来越多的系统资源，进而引起系统崩溃，这就是**雪崩效应**。
##### 服务熔断
1. 依赖
 ```xml
         <!--hystrix-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
 ```
2. yaml
   修改``eureka.instance.instance-id``即可
3. 修改Controller
   - `@HystrixCommand(fallbackMethod = xxx)`
   - 一旦调用服务方法失败并抛出错误信息之后，会自动调用HystrixCommand标注好的fallbackMethod调用类中指定方法
 ```java
 @RestController
public class DeptController {
    @Autowired
    private DeptService service;

    @RequestMapping("/dept/get/{id}")
    @HystrixCommand(fallbackMethod = "hystrixGet") //失败了就调用备选方案
    public Dept queryById(@PathVariable("id") Long id){
        Dept dept = service.queryById(id);

        if(dept == null){
            throw new RuntimeException("id->" + id + ",不存在该用户，请重试");
        }

        return dept;
    }

    //备选方案
    public Dept hystrixGet(Long id){
        return new Dept().
                setDeptno(id).
                setDname("id->" + id + " ユーザーは存在しません、もう一度試してください@hystrix").
                setDb_source("データベースは存在しません");
    }
}
 ```
4. 主启动类
   - ``@EnableCircuitBreaker`` 添加对熔断的支持

-->查询一条不存在的记录

![](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597680339316.png)

##### 服务降级
整体资源不够用了，先将某些服务先关掉，等渡过难关再重新开启

服务降级是**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**
**在客户端实现完成的，与服务端没有关系**


1. 根据已经有的DeptClientService接口，新建一个实现了FallbackFactory接口的类`DeptClientServiceFallbackFactory`，重写create方法

特别注意，必须加上`@Component`
```java
@Component 
public class DeptClientServiceFallbackFactory implements FallbackFactory {
    public Object create(Throwable throwable) {
        return new DeptClientService() {
            public Dept queryById(Long id) {
                return new Dept().
                        setDeptno(id).
                        setDname("id->" + id + "查找不到该用户信息，客户端已经降级，暂时不提供该服务@Hystrix").
                        setDb_source("No Service");
            }

            public List<Dept> queryAll() {
                return new ArrayList<Dept>();
            }

            public boolean addDept(Dept dept) {
                return false;
            }
        };
    }
}
```

2. 正如服务熔断，失败了需要调用一个备选方法。在这里也就是要让DeptClientServiceFallbackFactory 和 DeptClientService 发生关系

![发生性关系](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597682648559.png)

3. 需要在yaml开启
```yaml
#开启hystrix服务降级
feign:
  hystrix:
    enabled: true
```

-->假如此时我故意关闭Provider，然后去访问

![服务降级](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597682203276.png)

##### 熔断 和 降级 差异
+ 熔断：一般是指某个服务故障或者异常引起，类似现实世界的保险丝，当某个异常条件被触发，直接熔断整个服务，而不是一直等到该服务超时
+ 降级：一般是从整体符合考虑，就是当某个服务熔断之后，服务器不再被调用，**此时客户端自己准备一个本地的Fallback回调**，返回一个缺省值。这样虽然服务水平下降，但是好歹能用，比直接挂掉要强


##### 服务监控

Hystrix提供了调用监控（Hystrix DashBoard），Hystrix会持续记录所有通过Hystrix发起的请求的执行信息，并以统计报表和图形的形式展示给用户，包括每秒执行多少请求，多少成功，多少失败..

新建springcloud-**consumer**-hystrix-dashboard

1. 依赖
 ```xml
         <!--hystrix依赖-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix-dashboard</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
 ```
 
 2.yaml
```yaml
server:
  port: 9001
```

3. 主启动类添加`@EnableHystrixDashboard`
```java
@SpringBootApplication
@EnableHystrixDashboard //开启监控页面
public class DeptConsumerDashboard_9001 {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumerDashboard_9001.class, args);
    }
}
```

注意！所有Provider（8001/8002/8003）都需要监控依赖配置！
```xml
        <!--actuator：完成监控信息-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

启动springcloud-**consumer**-hystrix-dashboard监控，http://localhost:9001/hystrix

![http://localhost:9001/hystrix](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597686446895.png)

在springcloud-**provider**-dept-**hystrix**-8001的启动类添加一个Servlet！固定写法
```java
    //增加一个Servlet
    @Bean
    public ServletRegistrationBean registrationBean(){
        ServletRegistrationBean<HystrixMetricsStreamServlet> bean = new ServletRegistrationBean(new HystrixMetricsStreamServlet());

        bean.addUrlMappings("/actuator/hystrix.stream");

        return bean;
    }
```

通过访问 http://localhost:8001/actuator/hystrix.stream ，可以查看每1秒的数据流！

![http://localhost:8001//actuator/hystrix.stream](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597686623073.png)

可以通过可视化监控！

![可视化](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597686868728.png)

不断刷新http://localhost:8001/dept/get/1

![正常](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597686928974.png)

请求一个不存在数据多次，发现熔断了，爆红

![不正常](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597687048660.png)

---

### Zuul 路由网关
 提供**路由** + **过滤**
 
 1. 依赖
```xml
        <!--zuul-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zuul</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
		<!--eureka-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
```

2. yaml
   + 配置了路由访问映射规则，因为不想暴露我们真实的微服务地址！
   + 配置前通过访问**服务名** http://localhost:9527/springcloud-provider-dept/dept/get/1 
   + 配置后访问 http://localhost:9527/mydept/dept/get/1
```yaml
server:
  port: 9527

spring:
  application:
    name: springcloud-zuul-gateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/
  instance:
    instance-id: zuul9527.com
    prefer-ip-address: true

info:
  app.name: koshunho-springcloud
  company.name: Waseda Daigaku

#public void setRoutes(Map<String, ZuulProperties.ZuulRoute> routes) {
#这里是一个KV键值对，可以锤便定义，比如我就写mydept
zuul:
  routes:
    mydept.serviceId: springcloud-provider-dept
    mydept.path: /mydept/**
  ignored-services: springcloud-provider-dept #禁止使用该路径访问
```

3. 主启动类
![Zuul](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597743571710.png)

配置路径映射！注意此时已经通过Zuul提供的网关9527端口访问了。通过服务名访问不了了，因为我隐藏起来了

![](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597743744745.png)

所以现在只能通过我自定义的路径访问
![](https://raw.githubusercontent.com/koshunho/koshunhopic/master/xiaoshujiang/1597743918029.png)