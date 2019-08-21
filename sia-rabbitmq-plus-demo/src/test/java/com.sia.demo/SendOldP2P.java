package com.sia.demo;

import com.sia.rabbitmqplus.api.SIA;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;


public class SendOldP2P {
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
	// 发送的队列名称，必须指定，发送之前，确认接收者已启动
	private final static String QUEUE_NAME = "sia_client_test_send_p2p";

	public static void main(String[] argv) throws IOException, Exception {
		// log4j的配置，我们用slf4j统一接口的，可以指定其他的实现，比如logback
		PropertyConfigurator.configure("本地路径/conf/log4j.properties");
		// 指定配置文件的读取位置，默认在class路径下
		PropertyHelper.setProfilePath("本地路径/conf/");

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
