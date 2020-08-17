package com.huang.springcloud;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient //在服务启动后自动注册到Eureka中
@EnableCircuitBreaker //添加对熔断的支持
public class DeptProvider_Hystrix_8001 {
    public static void main(String[] args) {
        SpringApplication.run(DeptProvider_Hystrix_8001.class, args);
    }

    //增加一个Servlet
    @Bean
    public ServletRegistrationBean registrationBean(){
        ServletRegistrationBean<HystrixMetricsStreamServlet> bean = new ServletRegistrationBean(new HystrixMetricsStreamServlet());

        bean.addUrlMappings("/actuator/hystrix.stream");

        return bean;
    }
}
