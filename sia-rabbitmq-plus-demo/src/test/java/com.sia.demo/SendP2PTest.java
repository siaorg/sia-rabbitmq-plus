package com.sia.demo;

import com.sia.rabbitmqplus.api.SIA;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.io.IOException;


public class SendP2PTest extends BaseJunit {

    /**
     * 这是新提供的API，发送消息的类型为String（比如JSON格式）
     * 函数原型：
     * boolean send(String exchangeName, String queueName, String message)
     * 有两种模式支持：
     * 1，点对点模式，该模式下exchangeName设为null，指定发送的queueName
     * 注意，该模式下可能由于网络原因发送失败，注意处理失败情况
     * 2，发布订阅模式，该模式下指定发送的exchangeName，queueName设为""（空串）
     * 注意，该模式下没有反馈，只要不是网络问题，均能成功
     * 下面是点对点模式的实例
     * 发送的队列名称，必须指定，发送之前，确认接收者已启动
     */
    @Test
    public void testSendNewP2P() throws IOException, Exception {

        String QUEUE_NAME = "sia_client_test_send_p2p";

        int size = 9;
        while (true) {
            for (int i = size; i >= 0; i--) {
                // 发送的消息
                String msg = "" + i;
                // 注意处理失败 的情况，一般是由于网络原因断线重连，整个过程大概需要10秒钟才能恢复，注意与发布订阅模式相区别
                while (!SIA.send(null, QUEUE_NAME, msg))
                    Thread.sleep(10 * 1000);

                Thread.sleep(10);
            }

            Thread.sleep(10 * 1000);
        }
    }

    /**
     * 点对点模式
     * 函数原型：
     * boolean send(SIAMessage message)
     * 需要设置setBusinessCode，其实就是指定队列名称（注意与发布订阅模式下的setGroupCode相区别）
     * setMessageType(messageType)可以指定发送类型，以对象发送数据type为"object",则发送SIAMessage对象
     * 以字符串发送数据type为"text/plain",则只发送setMessageInfoClob中的消息
     * 默认为"text/plain"，发送字符串（与SIA3.0的发送类型一致）
     * 注意对发送失败的处理
     */
    @Test
    public void testSendOldP2P() throws IOException, Exception {

        // 发送的队列名称，必须指定，发送之前，确认接收者已启动
        String QUEUE_NAME = "sia_client_test_send_p2p";
        SIAMessage smessage = new SIAMessage();
        // 设置消息追踪的ID.可选
        smessage.setMessageId("a");
        //设置点对点模式下的队列名称，必须指定，注意与发布订阅模式相区别
        smessage.setBusinessCode(QUEUE_NAME);
        int size = 9;
        while (true) {
            for (int i = size; i >= 0; i--) {
                //以对象发送数据type为"object",以字符串发送数据type为"text/plain",默认为"object"，这个设置可选
                //smessage.setMessageType(messageType);
                //发送的消息
                smessage.setMessageInfoClob("" + i);
                // 注意处理失败 的情况，一般是由于网络原因断线重连，整个过程大概需要10秒钟才能恢复，注意与发布订阅模式相区别
                while (!SIA.send(smessage))
                    Thread.sleep(10 * 1000);

                Thread.sleep(10);
            }

            Thread.sleep(10 * 1000);
        }
    }


}
