package com.huang.springcloud.controller;

import com.huang.springcloud.pojo.Dept;
import com.huang.springcloud.service.DeptClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class DeptConsumerController {

    @Autowired
    private DeptClientService service;

    @RequestMapping("/consumer/dept/get/{id}")
    public Dept queryById(@PathVariable("id") Long id){
        return service.queryById(id);
    }

    @RequestMapping("/consumer/dept/list")
    public List<Dept> queryAll(){
        return service.queryAll();
    }

    @RequestMapping("/consumer/dept/add")
    public boolean addDept(Dept dept){
        return service.addDept(dept);
    };
}
