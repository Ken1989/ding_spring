package com.ding.service;

import com.springframework.annotation.Component;
import com.springframework.processor.BeanPostProcessor;
@Component
public class DingBeanPostProcessor implements BeanPostProcessor {
    @Override
    public void postProcessBeforeInitialization(String beanName, Object bean) {

    }

    @Override
    public void postProcessAfterInitialization(String beanName, Object bean) {
        System.out.println("postProcessAfterInitialization");
    }
}
