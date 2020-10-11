package com.ding.service;

import com.springframework.BeanNameAware;
import com.springframework.InitializingBean;
import com.springframework.annotation.Autowired;
import com.springframework.annotation.Component;
import com.springframework.annotation.Scope;

@Component("serviceB")
@Scope("singleton")
public class ServiceB implements BeanNameAware, InitializingBean {

    private String beanName;

    @Autowired
    private ServiceA serviceA;

    @Override
    public void setBeanName(String name) {
        System.out.println(name);
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("---> Instance");
    }
}
