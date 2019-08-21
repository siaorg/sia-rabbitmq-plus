package com.sia.demo;

import com.sia.rabbitmqplus.api.SIA;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.apache.log4j.PropertyConfigurator;


public class beginSend {

	/**
	 * 同步链条模式-起始发送
	 * 首先意识到同步链条模式其实是用双工异步来模拟同步
	 * 然后知道此模式下，起始发送与终止发送只能有一个，而且必须有，而中间发送可以有多个，也可以没有
	 * 整个通信流程如下：
	 * 起始发送发送一条消息A到下一端，同时开始监听回调队列，消息经过N（N>=0）个中间发送的处理并转发（A1，A2，...AN），最终由终止发送响应，往回调队列回复消息B
	 * 注意初始发送的监听是有过期时间的（这样才是同步呐，有严格的响应时间限制）
	 * beginSend->middleSend(1)->middleSend(2)->middleSend(...)->middleSend(N)->endSend
	 * 		|																		| response				
	 *    listen <------------------------------------------------------------------+
	 * 从整个过程来看，同步链条模式下的各个节点既是发送者又是接收者
	 * 不同的是，起始发送不用启动消费线程（其实内部启动了）
	 * 中间发送与终止发送必须启动消费线程才能接收消息  
	 */
	
	private final static String QUEUE_NAME = "sia_client_test_begin2Middle";
	

	@SuppressWarnings("deprecation")
	public static void main(String[] argv) throws Exception {
		// log4j的配置，我们用slf4j统一接口的，可以指定其他的实现，比如logback
		PropertyConfigurator.configure("本地路径/conf/log4j.properties");
		// 指定配置文件的读取位置，默认在class路径下
		PropertyHelper.setProfilePath("本地路径/conf/");

		SIAMessage message = new SIAMessage();
		//设置发送的队列名称，必须指定
		message.setBusinessCode(QUEUE_NAME);
		
		String res = null;
		int size = 10;
		for (int i = 0; i < size; i++) {
			//发送的消息
			message.setMessageInfoClob("begin " + i);
			//注意处理失败的情况：如果因为网络原因或链条模式超时，则返回值为null
			do {
				res = SIA.beginSend((message));
				if (res == null)
					Thread.sleep(5 * 1000);
			} while (res == null);
			System.out.println(res);
			Thread.sleep(1000);
		}
	}
}
