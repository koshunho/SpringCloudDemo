package com.huang.springcloud.controller;

import com.huang.springcloud.pojo.Dept;
import com.huang.springcloud.service.DeptService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


// 提供RestFul服务
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
