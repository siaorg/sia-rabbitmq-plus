package com.sia.demo;


import com.sia.rabbitmqplus.binding.Consumer;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


public class testBean {
	
	public void getMessage(SIAMessage message) {
		System.out.println(message.getMessageInfoClob());
	}

	public void getMessage(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) {
		// log4j的配置，我们用slf4j统一接口的，可以指定其他的实现，比如logback
		PropertyConfigurator.configure("本地路径/conf/log4j.properties");
		// 指定配置文件的读取位置，默认在class路径下
		PropertyHelper.setProfilePath("本地路径/conf/bean/");
	
		/**web.xml的最后，添加SIA的listener，效果与下面的操作等同
		 * <listener> 
		 * 	<listener-class>com.creditease.sia.start.SIAInitialListener</listener-class> 
		 * </listener>
		 */
		
		// 读文件的方式加载Spring
		ApplicationContext app = new FileSystemXmlApplicationContext("本地路径/conf/bean/beans.xml");
		// 传递Spring的上下文（还可以指定listener）
		Consumer.start(app);

	}
}
