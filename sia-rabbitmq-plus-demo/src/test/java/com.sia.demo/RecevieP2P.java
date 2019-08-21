package com.sia.demo;

import com.sia.rabbitmqplus.binding.Consumer;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.apache.log4j.PropertyConfigurator;


public class RecevieP2P {

	/**
	 * 这是点对点模式下的接收者
	 * 
	 * 建议利用重载机制写两个接收函数（处理逻辑一致，接收参数不同），以防接收消息类型不匹配（SIA利用类反射获取处理函数）导致丢失
	 * 
	 * 建议消息处理用try catch 包裹，自己处理异常(认真对待你的消息)
	 * 
	 * @param message
	 */
	public void execRun(String message) {
		try {
			System.out.println(message);
			Thread.sleep(5000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void execRun(SIAMessage message) {

		try {
			System.out.println(message.getMessageInfoClob());
			Thread.sleep(5000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] argv) throws Exception {
		// log4j的配置，我们用slf4j统一接口的，可以指定其他的实现，比如logback
		PropertyConfigurator.configure("/Users/ifeng/IdeaProjects/sia-rabbitmq-plus/sia-rabbitmq-plus-demo/src/test/java/com.sia.demo/conf/log4j.properties");
		// 指定配置文件的读取位置，默认在class路径下
		PropertyHelper.setProfilePath("/Users/ifeng/IdeaProjects/sia-rabbitmq-plus/sia-rabbitmq-plus-demo/src/test/java/com.sia.demo/conf/RecevieP2P");
		//启动消费者
		Consumer.start();
	}

}
