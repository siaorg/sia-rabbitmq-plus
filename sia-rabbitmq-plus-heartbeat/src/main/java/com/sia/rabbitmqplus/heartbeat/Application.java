package com.sia.rabbitmqplus.heartbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by xinliang on 16/11/14.
 */
@SpringBootApplication
@EnableScheduling
@EnableEurekaClient
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
        LOGGER.info("sia-rabbitmq-plus-heartbeat启动！");
    }

}
