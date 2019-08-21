package com.sia.demo;

import com.sia.rabbitmqplus.binding.PropertyHelper;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;

/**
 * @author ifeng
 * @version 1.0
 * @date 2019/8/21下午5:34
 **/
public class BaseJunit {



    @Before
    public void init(){
        // log4j的配置，我们用slf4j统一接口的，可以指定其他的实现，比如logback
        PropertyConfigurator.configure("/Users/ifeng/IdeaProjects/sia-rabbitmq-plus/sia-rabbitmq-plus-demo/src/test/java/com.sia.demo/conf/log4j.properties");
        // 指定配置文件的读取位置，默认在class路径下
        PropertyHelper.setProfilePath("/Users/ifeng/IdeaProjects/sia-rabbitmq-plus/sia-rabbitmq-plus-demo/src/test/java/com.sia.demo/conf/");
    }
}
