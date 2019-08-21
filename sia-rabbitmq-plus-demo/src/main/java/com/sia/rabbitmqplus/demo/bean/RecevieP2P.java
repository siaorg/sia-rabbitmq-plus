package com.sia.rabbitmqplus.demo.bean;

import com.sia.rabbitmqplus.binding.Consumer;
import com.sia.rabbitmqplus.binding.PropertyHelper;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author lipengfei
 */
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

}
