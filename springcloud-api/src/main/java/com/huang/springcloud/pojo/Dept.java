package com.huang.springcloud.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true) //链式写法
public class Dept implements Serializable {
    private Long deptno;

    private String dname;

    private String db_source;

    public Dept(String dname) {
        this.dname = dname;
    }

    /*
    * 链式写法：
    * Dept dept = new Dept();
    *
    * dept.setDeptno(11).setDname("ssss").setDb_source("001");*/
}
