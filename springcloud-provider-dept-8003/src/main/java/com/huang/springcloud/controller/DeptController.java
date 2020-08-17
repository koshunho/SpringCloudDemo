package com.huang.springcloud.controller;

import com.huang.springcloud.pojo.Dept;
import com.huang.springcloud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 提供RestFul服务
@RestController
public class DeptController {
    @Autowired
    private DeptService deptService;

    // 获取一些配置的信息，得到具体的微服务
    @Autowired
    private DiscoveryClient client;


    // 注意要加上@RequestBody：直接以String接收前端传过来的Json数据
    @RequestMapping("/dept/add")
    public boolean addDept(@RequestBody Dept dept){
        return deptService.addDept(dept);
    }

    @GetMapping("/dept/get/{id}")
    public Dept get(@PathVariable("id") Long id){
        return deptService.queryById(id);
    }

    @GetMapping("/dept/list")
    public List<Dept> queryAll(){
        return deptService.queryAll();
    }

    //注册进来的微服务，可以通过获取一些这个服务的一些信息
    @RequestMapping("/dept/discovery")
    public Object discovery(){
        //获得微服务列表的清单
        List<String> services = client.getServices();
        System.out.println("discovery->services:" + services);

        //得到一个具体的微服务信息，通过具体的微服务ID, ApplicationName
        List<ServiceInstance> instances = client.getInstances("SPRINGCLOUD-PROVIDER-DEPT");
        for (ServiceInstance instance : instances) {
            System.out.println(
                    instance.getHost()+"\t"+
                    instance.getPort()+"\t"+
                    instance.getUri()+"\t"+
                    instance.getServiceId()
            );
        }
        return this.client;
    }
}
