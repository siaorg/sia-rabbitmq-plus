package com.sia.rabbitmqplus.demo;

import com.sia.rabbitmqplus.binding.Consumer;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


public class ReceviePubSub {

    /**
     * 这是发布订阅模式下的接收者
     * 
     * 建议利用重载机制写两个接收函数（处理逻辑一致，接收参数不同），以防接收消息类型不匹配（SIA利用类反射获取处理函数）导致丢失
     * 
     * 建议消息处理用try catch 包裹，自己处理异常(认真对待你的消息)
     * 
     * 在发布订阅模式下，同一个消息会推送到每一个绑定到exchangeName的队列中。
     * 
     * 比如EXCHANGE_NAME为skytrain_client_test_send_pubsub，与它绑定的队列有：
     * skytrain_client_test_receive_message1与skytrain_client_test_receive_message2， 则这两个队列会接收到同样的消息。
     * 
     * 
     * @param message
     */
    public void execRun(String message) {

        try {
            System.out.println(message);
            Thread.sleep(5000);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execRun(SIAMessage message) {

        try {
            System.out.println(message.getMessageInfoClob());
            Thread.sleep(5000);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] argv) throws Exception {

        Properties props = new Properties();
        System.out.println(ReceviePubSub.class.getClassLoader().getResource(""));
        System.out.println(new File(ReceviePubSub.class.getClassLoader().getResource("log5j.properties").getPath()));
        InputStream inputStream = new FileInputStream(new File(ReceviePubSub.class.getClassLoader().getResource("conf/log4j.properties").getPath()));
        props.load(new ReceviePubSub().getClass().getResourceAsStream("/test/conf/log4j.properties"));
        // log4j的配置，我们用slf4j统一接口的，可以指定其他的实现，比如logback
        PropertyConfigurator.configure(props);
        // 指定配置文件的读取位置，默认在class路径下
        PropertyHelper.setProfilePath("../conf/ReceviePubSub");
        // 启动消费者
        Consumer.start();
    }

}
