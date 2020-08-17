package com.huang.springcloud.service;

import com.huang.springcloud.pojo.Dept;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
