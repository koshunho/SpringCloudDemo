package com.huang.springcloud.service;

import com.huang.springcloud.pojo.Dept;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "SPRINGCLOUD-PROVIDER-DEPT", fallbackFactory = DeptClientServiceFallbackFactory.class)
public interface DeptClientService {

    @RequestMapping("/dept/get/{id}")
    Dept queryById(@PathVariable("id") Long id);

    @RequestMapping("/dept/list")
    List<Dept> queryAll();

    @RequestMapping("/dept/add")
    boolean addDept(Dept dept);
}
