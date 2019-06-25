package com.sia.rabbitmqplus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * Created by xinliang on 2017/10/26.
 */
//@EnableScheduling
@SpringBootApplication(scanBasePackages = { "com.sia", "com.sia.rabbitmqplus"})
@EnableEurekaClient
@EnableFeignClients
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
        LOGGER.info("sia-rabbitmq-plus-gather启动！");
    }

}
