package com.huang.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ribbon 和 Eureka 整合以后，客户端可以直接调用，不用关心IP和端口号，也就是可以直接通过服务名来访问provider
@SpringBootApplication
public class DeptConsumer_81 {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer_81.class, args);
    }
}
