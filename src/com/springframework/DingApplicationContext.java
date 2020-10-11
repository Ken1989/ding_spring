package com.springframework;

import com.springframework.annotation.Autowired;
import com.springframework.annotation.Component;
import com.springframework.annotation.ComponentScan;
import com.springframework.annotation.Scope;
import com.springframework.processor.BeanPostProcessor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DingApplicationContext {

    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    /**
     * Constructor with ConfigClass
     * @param configClass
     */
    public DingApplicationContext(Class configClass) {
        this.scan(configClass);

        // instanceSingletonBean
        instanceSingletonBean();

    }


    /**
     * instance singleton bean (Non-Lazy)
     * 1. Initialization
     */
    private void instanceSingletonBean() {

        for (String beanName : this.beanDefinitionMap.keySet()) {

            BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

            if(ScopeEnum.singleton.equals(beanDefinition.getScope())) {
                Object bean = doCreateBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }


    /**
     * Create bean by beanDefinition
     * @param beanName
     * @param beanDefinition
     * @return
     */
    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {

        Class beanClass = beanDefinition.getBeanclass();

        try {

            /**
             * GetDeclaredConstructor can get all of constructor that include
             * public, private, no-parameters etc...
             * So, remember to use getDeclaredFields
             */
            Constructor constructor = beanClass.getDeclaredConstructor();
            Object instance = constructor.newInstance();

            // Attribute inject
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {

                if(field.isAnnotationPresent(Autowired.class)) {
                    String filedName = field.getName();
                    Object bean = this.getBean(filedName);
                    /**
                     * In Java, the field value in the entity class can be obtained through reflection.
                     * When the setAccessible method of Field is not set to true,
                     * the access security check will be performed when calling,
                     * and an IllegalAccessException will be thrown.
                     */
                    field.setAccessible(true);
                    field.set(instance,bean);

                }
            }

            // Call Aware
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            if (instance instanceof InitializingBean) {
                ((InitializingBean)instance).afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : this.beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            }

            return instance;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Get bean from beanMap, if not found for this bean
     * Then it will call deCreateBean function
     * @param filedName
     * @return
     */
    public Object getBean(String filedName) {
        if(this.singletonObjects.containsKey(filedName)) {
            return singletonObjects.get(filedName);
        } else {
            return this.doCreateBean(filedName, this.beanDefinitionMap.get(filedName));
        }
    }

    private void scan(Class configClass) {
        // Get package path
        ComponentScan componentScan = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String packagePath = componentScan.value();

        // Get ClassBean list by scanning package
        List<Class> classList = this.getBeanClasses(packagePath);

        // Generate BeanDefinition
        for (Class clazz : classList) {
            // Check the component annotation
            if (clazz.isAnnotationPresent(Component.class)) {
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanclass(clazz);

                Component component = (Component) clazz.getAnnotation(Component.class);
                String beanName = component.value();

                // Parse BeanPostProcessor
                if(BeanPostProcessor.class.isAssignableFrom(clazz)) {
                    try {
                        BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getConstructor().newInstance();
                        this.beanPostProcessorList.add(beanPostProcessor);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }

                // Parse Scope
                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scope = (Scope) clazz.getAnnotation(Scope.class);
                    String scopeValue = scope.value();
                    if(ScopeEnum.singleton.name().equals(scopeValue)) {
                        beanDefinition.setScope(ScopeEnum.singleton);
                    } else {
                        beanDefinition.setScope(ScopeEnum.prototype);
                    }
                } else {
                    beanDefinition.setScope(ScopeEnum.singleton);
                }


                this.beanDefinitionMap.put(beanName, beanDefinition);

            }
        }
    }


    /**
     * Here we need to get the APP classloader for loading our class files
     * For our applications, all of class files will be saved in target or out folder
     * When we get class file list, then we need to create instance by class
     * Classloader only can identify full name of Class like com/ding/service/ServiceB
     * So we need to substring the fileName then replace // to .
     * @Author Ding, Ke
     * @param packagePath
     * @return
     */
    private List<Class> getBeanClasses(String packagePath) {

        List<Class> beanClasses = new ArrayList<>();

        ClassLoader classLoader = DingApplicationContext.class.getClassLoader();
        packagePath = packagePath.replace(".","/");
        URL resource = classLoader.getResource(packagePath);
        File file = new File(resource.getFile());

        if(file.isDirectory()) {
            for (File class_file : file.listFiles()) {
                String fileName = class_file.getAbsolutePath();
                if (fileName.endsWith(".class")) {
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("/", "."); // Linux or Mac, using "/" not "//"

                    try {
                        Class clazz = classLoader.loadClass(className);
                        beanClasses.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return beanClasses;
    }
}
