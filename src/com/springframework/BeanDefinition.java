package com.springframework;

public class BeanDefinition {

    private Class beanclass;
    private ScopeEnum scope;

    public Class getBeanclass() {
        return beanclass;
    }

    public void setBeanclass(Class beanclass) {
        this.beanclass = beanclass;
    }

    public ScopeEnum getScope() {
        return scope;
    }

    public void setScope(ScopeEnum scope) {
        this.scope = scope;
    }
}
