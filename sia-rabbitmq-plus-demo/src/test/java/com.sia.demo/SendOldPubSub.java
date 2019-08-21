package com.sia.demo;

import com.sia.rabbitmqplus.api.SIA;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;


public class SendOldPubSub {
	/**
	 * 发布订阅模式
	 * 函数原型：
	 * void sendGroup(SIAMessage message)
	 * 需要设置setGroupCode，其实就是指定交换机名称（与点对点模式下的setBusinessCode注意区别）
	 * setMessageType(messageType)可以指定发送类型，以对象发送数据type为"object",则发送SIAMessage对象
	 * 以字符串发送数据type为"text/plain",则只发送setMessageInfoClob中的消息
	 * 默认为"text/plain"，发送字符串（与SIA3.0的发送类型一致）
	 * 注意对发送失败的处理
	 */
	// 交换机的名称，必须指定，在发送之前，确认接收者已启动！
	private final static String EXCHANGE_NAME = "sia_client_test_send_pubsub";

	public static void main(String[] argv) throws IOException, Exception {
		// log4j的配置，我们用slf4j统一接口的，可以指定其他的实现，比如logback
		PropertyConfigurator.configure("本地路径/conf/log4j.properties");
		// 指定配置文件的读取位置，默认在class路径下
		PropertyHelper.setProfilePath("本地路径/conf/");

		SIAMessage smessage = new SIAMessage();
		// 设置消息追踪的ID.可选
		smessage.setMessageId("a");
		//设置发布订阅模式下的交换机名称，必须指定，注意与点对点模式相区别
		smessage.setGroupCode(EXCHANGE_NAME);
		
		int size = 9;
		while (true) {
			for (int i = size; i >= 0; i--) {
				//以对象发送数据type为"object",以字符串发送数据type为"text/plain",默认为"object"，这个设置可选
				//smessage.setMessageType("text/plain");
				//发送的消息
				smessage.setMessageInfoClob("" + i);
				//发布订阅模式的发送（广播）
				SIA.sendGroup(smessage);
					
				Thread.sleep(1000);
			}

			Thread.sleep(10 * 1000);
		}
	}
}
