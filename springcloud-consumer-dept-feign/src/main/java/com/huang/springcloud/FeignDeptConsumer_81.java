package com.huang.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

// Ribbon 和 Eureka 整合以后，客户端可以直接调用，不用关心IP和端口号，也就是可以直接通过服务名来访问provider
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients //就能扫描到springcloud-api写的DeptClientServiceLe
public class FeignDeptConsumer_81 {
    public static void main(String[] args) {
        SpringApplication.run(FeignDeptConsumer_81.class, args);
    }
}
