package main;

import com.ding.config.AppConfig;
import com.ding.service.ServiceB;
import com.springframework.DingApplicationContext;

public class App {

    public static void main(String[] args) {

        DingApplicationContext dingApplicationContext = new DingApplicationContext(AppConfig.class);
        ServiceB serviceB = (ServiceB) dingApplicationContext.getBean("serviceB");
        System.out.println(serviceB);
    }
}
