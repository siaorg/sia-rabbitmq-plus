package com.sia.rabbitmqplus.api;

import com.sia.rabbitmqplus.pojo.SIAMessage;
import com.sia.rabbitmqplus.binding.Const;
import com.sia.rabbitmqplus.binding.Provider;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lipengfei
 */
public class SIA {

	private static final Logger LOGGER = LoggerFactory.getLogger(SIA.class);
	private static final Provider PROVIDER = new Provider();

	private SIA() {

	}

	/**
	 * <p/>
	 * 这是新提供的API，发送消息的类型为String（比如JSON格式）
	 * <p/>
	 * 函数原型： boolean send(String exchangeName, String queueName, String message)
	 * <p/>
	 * 有两种模式支持：
	 * <p/>
	 * 1，点对点模式，该模式下exchangeName设为null，指定发送的queueName
	 * 注意，该模式下可能由于网络原因发送失败，注意处理失败情况
	 * <p/>
	 * 2，发布订阅模式，该模式下指定发送的exchangeName，queueName设为""（空串）
	 * <p/>
	 * 注意，该模式下没有反馈，只要不是网络问题，均能成功
	 *
	 * @param exchangeName
	 * @param queueName
	 * @param message
	 * @return
	 */
	public static boolean send(String exchangeName, String queueName, String message) {
		try {
			PROVIDER.send(exchangeName, queueName, message);
		} catch (Exception ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX, ex);
			return false;
		}
		return true;
	}

	/**
	 * <p/>
	 * 点对点模式
	 * <p/>
	 * 函数原型： boolean send(SIAMessage message)
	 * <p/>
	 * 需要设置setBusinessCode，其实就是指定队列名称（注意与发布订阅模式下的setGroupCode相区别）
	 * <p/>
	 * setMessageType(messageType)可以指定发送类型， 默认为"text/plain"
	 * <p/>
	 * 以对象发送数据type为"object"，则发送SIAMessage对象
	 * <p/>
	 * 以字符串发送数据type为"text/plain"，则只发送setMessageInfoClob中的消息
	 * <p/>
	 * 注意对发送失败的处理
	 */
	public static boolean send(SIAMessage message) {
		try {
			if (null == message.getMessageType() || "".equalsIgnoreCase(message.getMessageType())) {
				message.setMessageType("text/plain");
				LOGGER.info(Const.SIA_LOG_PREFIX
						+ "以对象发送数据type为[object]，则发送SIAMessage对象;以字符串发送数据type为[text/plain]，则只发送setMessageInfoClob中的消息");
				LOGGER.warn(Const.SIA_LOG_PREFIX + "方法[send(SIAMessage message)]没有设置发送消息类型，系统给定默认值为:["
						+ message.getMessageType() + "]");
			}
			PROVIDER.send(message);
		} catch (Exception ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX, ex);
			return false;
		}
		return true;
	}

	/**
	 * @deprecated 同步链条模式-起始发送
	 *             <p/>
	 *             首先意识到同步链条模式其实是用双工异步来模拟同步
	 *             <p/>
	 *             然后知道此模式下，起始发送与终止发送只能有一个，而且必须有，而中间发送可以有多个，也可以没有
	 *             <p/>
	 *             整个通信流程如下：
	 *             <p/>
	 *             起始发送发送一条消息A到下一端，同时开始监听回调队列，消息经过N（N>=0）个中间发送的处理并转发（A1，A2，...AN
	 *             ），最终由终止发送响应，往回调队列回复消息B 注意初始发送的监听是有过期时间的（这样才是同步呐，有严格的响应时间限制）
	 *             <p/>
	 * 
	 *             <pre>
	 * {@code
	 * beginSend->middleSend(1)->middleSend(2)->middleSend(...)->middleSend(N)->endSend
	 *      |                                                                       | response
	 *    listen <------------------------------------------------------------------+
	 * }
	 *             </pre>
	 *
	 *             从整个过程来看，同步链条模式下的各个节点既是发送者又是接收者
	 *
	 *             不同的是，起始发送不用启动消费线程（其实内部启动了）
	 *
	 *             中间发送与终止发送必须启动消费线程才能接收消息
	 */
	@Deprecated
	public static String beginSend(SIAMessage message) {
		message.setReceiveQueueName(UUID.randomUUID().toString());
		try {
			if (null == message.getMessageType() || "".equalsIgnoreCase(message.getMessageType())) {
				message.setMessageType("object");
				LOGGER.info(Const.SIA_LOG_PREFIX
						+ "以对象发送数据type为[object]，则发送SIAMessage对象;以字符串发送数据type为[text/plain]，则只发送setMessageInfoClob中的消息");
				LOGGER.warn(Const.SIA_LOG_PREFIX + "方法[SIA.beginSend(SIAMessage message)]没有设置发送消息类型，系统给定默认值为:["
						+ message.getMessageType() + "]");
			}
			LOGGER.info(Const.SIA_LOG_PREFIX + "同步链条模式的回调队列名为:[" + message.getReceiveQueueName() + "]");
			PROVIDER.beginSend(message);
			return PROVIDER.receive(message);
		} catch (Exception ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "beginSend", ex);
		}
		return null;
	}

	/**
	 * @deprecated 同步链条模式-中间发送
	 *             <p/>
	 *             首先意识到同步链条模式其实是用双工异步来模拟同步
	 *             <p/>
	 *             然后知道此模式下，起始发送与终止发送只能有一个，而且必须有，而中间发送可以有多个，也可以没有
	 *             <p/>
	 *             整个通信流程如下：
	 *             <p/>
	 *             起始发送发送一条消息A到下一端，同时开始监听回调队列，消息经过N（N>=0）个中间发送的处理并转发（A1，A2，...AN
	 *             ），最终由终止发送响应，往回调队列回复消息B 注意初始发送的监听是有过期时间的（这样才是同步呐，有严格的响应时间限制）
	 *             <p/>
	 * 
	 *             <pre>
	 * {@code
	 * beginSend->middleSend(1)->middleSend(2)->middleSend(...)->middleSend(N)->endSend
	 *      |                                                                       | response
	 *    listen <------------------------------------------------------------------+
	 * }
	 *             </pre>
	 *
	 *             从整个过程来看，同步链条模式下的各个节点既是发送者又是接收者
	 *
	 *             不同的是，起始发送不用启动消费线程（其实内部启动了）
	 *
	 *             中间发送与终止发送必须启动消费线程才能接收消息
	 */
	@Deprecated
	public static boolean middleSend(SIAMessage message) {
		try {
			if (null == message.getMessageType() || "".equalsIgnoreCase(message.getMessageType())) {
				message.setMessageType("object");
				LOGGER.info(Const.SIA_LOG_PREFIX
						+ "以对象发送数据type为[object]，则发送SIAMessage对象;以字符串发送数据type为[text/plain]，则只发送setMessageInfoClob中的消息");
				LOGGER.warn(Const.SIA_LOG_PREFIX + "方法[SIA.middleSend(SIAMessage message)]没有设置发送消息类型，系统给定默认值为:["
						+ message.getMessageType() + "]");
			}
			LOGGER.info(Const.SIA_LOG_PREFIX + "同步链条模式的回调队列名为:[" + message.getReceiveQueueName() + "]");
			PROVIDER.middleSend(message);
		} catch (Exception ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "middleSend", ex);
			return false;
		}
		return true;
	}

	/**
	 * @deprecated 同步链条模式-终止发送
	 *             <p/>
	 *             首先意识到同步链条模式其实是用双工异步来模拟同步
	 *             <p/>
	 *             然后知道此模式下，起始发送与终止发送只能有一个，而且必须有，而中间发送可以有多个，也可以没有
	 *             <p/>
	 *             整个通信流程如下：
	 *             <p/>
	 *             起始发送发送一条消息A到下一端，同时开始监听回调队列，消息经过N（N>=0）个中间发送的处理并转发（A1，A2，...AN
	 *             ），最终由终止发送响应，往回调队列回复消息B 注意初始发送的监听是有过期时间的（这样才是同步呐，有严格的响应时间限制）
	 *             <p/>
	 * 
	 *             <pre>
	 * {@code
	 * beginSend->middleSend(1)->middleSend(2)->middleSend(...)->middleSend(N)->endSend
	 *      |                                                                       | response
	 *    listen <------------------------------------------------------------------+
	 * }
	 *             </pre>
	 *
	 *             从整个过程来看，同步链条模式下的各个节点既是发送者又是接收者
	 *
	 *             不同的是，起始发送不用启动消费线程（其实内部启动了）
	 *
	 *             中间发送与终止发送必须启动消费线程才能接收消息
	 */
	@Deprecated
	public static boolean endSend(SIAMessage message) {
		try {
			if (null == message.getMessageType() || "".equalsIgnoreCase(message.getMessageType())) {
				message.setMessageType("object");
				LOGGER.info(Const.SIA_LOG_PREFIX
						+ "以对象发送数据type为[object]，则发送SIAMessage对象;以字符串发送数据type为[text/plain]，则只发送setMessageInfoClob中的消息");
				LOGGER.warn(Const.SIA_LOG_PREFIX + "方法[SIA.endSend(SIAMessage message)]没有设置发送消息类型，系统给定默认值为:["
						+ message.getMessageType() + "]");
			}
			LOGGER.info(Const.SIA_LOG_PREFIX + "同步链条模式的回调队列名为:[" + message.getReceiveQueueName() + "]");
			PROVIDER.endSend(message);
		} catch (Exception ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "endSend", ex);
			return false;
		}
		return true;
	}

	/**
	 * <p/>
	 * 发布订阅模式
	 * <p/>
	 * 函数原型： void sendGroup(SIAMessage message)
	 * <p/>
	 * 需要设置setGroupCode，其实就是指定交换机名称（与点对点模式下的setBusinessCode注意区别）
	 * <p/>
	 * setMessageType(messageType)可以指定发送类型， 默认为"text/plain"
	 * <p/>
	 * 以对象发送数据type为"object"，则发送SIAMessage对象
	 * <p/>
	 * 以字符串发送数据type为"text/plain"，则只发送setMessageInfoClob中的消息
	 * <p/>
	 * <p/>
	 * 注意对发送失败的处理
	 */
	public static void sendGroup(SIAMessage message) {
		try {
			if (null == message.getMessageType() || "".equalsIgnoreCase(message.getMessageType())) {
				message.setMessageType("text/plain");
				LOGGER.info(Const.SIA_LOG_PREFIX
						+ "以对象发送数据type为[object]，则发送SIAMessage对象;以字符串发送数据type为[text/plain]，则只发送setMessageInfoClob中的消息");
				LOGGER.warn(Const.SIA_LOG_PREFIX + "方法[SIA.sendGroup(SIAMessage message]没有设置发送消息类型，系统给定默认值为:["
						+ message.getMessageType() + "]");
			}
			PROVIDER.send(message);
		} catch (Exception ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "sendGroup", ex);
		}
	}

}
