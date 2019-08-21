package com.sia.demo;

import com.sia.rabbitmqplus.api.SIA;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;


public class SendNewPubSub {
	/**
	 * 这是新提供的API，发送消息的类型为String（比如JSON格式）
	 * 函数原型：
	 * boolean send(String exchangeName, String queueName, String message)
	 * 有两种模式支持：
	 * 1，点对点模式，该模式下exchangeName设为null，指定发送的queueName
	 * 注意，该模式下可能由于网络原因发送失败，注意处理失败情况
	 * 2，发布订阅模式，该模式下指定发送的exchangeName，queueName设为""（空串）
	 * 注意，该模式下没有反馈，只要不是网络问题，均能成功
	 * 下面是发布订阅模式的实例
	 */
	// 交换机的名称，必须指定，在发送之前，确认接收者已启动！
	private final static String EXCHANGE_NAME = "sia_client_test_send_pubsub";

	public static void main(String[] argv) throws IOException, Exception {
		// log4j的配置，我们用slf4j统一接口的，可以指定其他的实现，比如logback
		PropertyConfigurator.configure("本地路径/conf/log4j.properties");
		// 指定配置文件的读取位置，默认在class路径下
		PropertyHelper.setProfilePath("本地路径/conf/");

		int size = 9;

		while (true) {
			for (int i = size; i >= 0; i--) {
				//发送的消息
				String msg = "" + i;
				//注意与点对点模式相区别
				SIA.send(EXCHANGE_NAME, "", msg);
				
				Thread.sleep(1000);
			}

			Thread.sleep(10 * 1000);
		}
	}
}
