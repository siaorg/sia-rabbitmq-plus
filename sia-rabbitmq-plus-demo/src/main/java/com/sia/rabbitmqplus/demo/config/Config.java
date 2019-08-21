package com.sia.rabbitmqplus.demo.config;

import com.sia.rabbitmqplus.demo.bean.RecevieP2P;
import com.sia.rabbitmqplus.demo.bean.SendP2P;
import com.sia.rabbitmqplus.start.SIAInitialListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author ifeng
 * @version 1.0
 * @date 2019/8/15下午5:20
 **/
@Configuration
public class Config extends WebMvcConfigurerAdapter {

    /**
     * 启动消费者，注册监听
     * @return
     */
    @Bean
    public ServletListenerRegistrationBean listenerRegist() {
        ServletListenerRegistrationBean srb = new ServletListenerRegistrationBean();
        srb.setListener(new SIAInitialListener());
        return srb;
    }

    /**
     * 这个是测试点对点发送的方式，通过new对象的时候加载方法运行发送方法，方法内容和test包下main方法相同
     * 如需测试其他方式可以到test包下通过单独运行main方法运行，需改配置文件本地路径
     * @return
     */
    @Bean
    SendP2P sendP2P(){
      return   new  SendP2P();
    }
}
