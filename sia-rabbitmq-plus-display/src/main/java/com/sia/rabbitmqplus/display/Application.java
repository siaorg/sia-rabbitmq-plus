package com.sia.rabbitmqplus.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 *  @author xinliang
 *  @date 2017/9/10
 */
@SpringBootApplication
@Controller
@ComponentScan("com.sia")
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @RequestMapping("/")
    public String index() {

        return "uavapp_sia/msgmonitor/main";
    }

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
        LOGGER.info("sia-rabbitmq-plus-display启动！");
    }

}
