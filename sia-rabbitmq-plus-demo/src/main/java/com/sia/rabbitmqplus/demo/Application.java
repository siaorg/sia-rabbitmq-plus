package com.sia.rabbitmqplus.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ifeng
 * @version 1.0
 * @date 2019/8/15下午4:56
 **/
@SpringBootApplication
@ComponentScan("com.sia")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
