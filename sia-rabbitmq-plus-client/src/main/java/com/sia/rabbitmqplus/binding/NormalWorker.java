package com.sia.rabbitmqplus.binding;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Consumer;
import com.sia.rabbitmqplus.business.ExecThreadPool;
import com.sia.rabbitmqplus.log.SkyTrainLogger;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xinliang
 * @date 16/8/8
 */
public class NormalWorker implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(NormalWorker.class);
	private static final ConcurrentHashMap<String,Object> INSTANCE=new ConcurrentHashMap<String,Object>();
	private String exchangeName;
	private String queueName;
	private String className;
	private String methodName;
	private boolean autoAck;

	/**
	 * structure
	 *
	 * @param exchangeName
	 * @param queueName
	 * @param className
	 * @param methodName
	 */
	public NormalWorker(String exchangeName, String queueName, String className, String methodName, boolean autoAck) {
		this.exchangeName = exchangeName;
		this.queueName = queueName;
		this.className = className;
		this.methodName = methodName;
		this.autoAck = autoAck;
	}

	/**
	 * use thread pool execute task
	 */
	@Override
	public void run() {

		final Channel channel = ChannelPool.open();
		LOGGER.info(Const.SIA_LOG_PREFIX + queueName + " 队列开始监听");
		ClientDataGather.addDeliverDate(queueName);
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				byte[] bytes = body;
				long tag = envelope.getDeliveryTag();
				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					ObjectInputStream ois = new ObjectInputStream(bais);
					Object message = ois.readObject();
					if (autoAck) {
						ExecThreadPool.execute(queueName, new NormalWorker.NormalTask(exchangeName, queueName, message,
								className, methodName, null, tag));
					} else {
						ExecThreadPool.execute(queueName, new NormalWorker.NormalTask(exchangeName, queueName, message,
								className, methodName, channel, tag));
					}

					SkyTrainLogger.log(queueName, "[receive-SIAMessage]->" + message);
				} catch (java.lang.ClassNotFoundException ex) {
					LOGGER.error(Const.SIA_LOG_PREFIX, ex);
				} catch (java.io.IOException ex) {
					String message = new String(bytes, Charset.forName("UTF-8"));
					LOGGER.info(Const.SIA_LOG_PREFIX + "非SIAMessage对象，转化为String对象:" + message);
					if (autoAck) {
						ExecThreadPool.execute(queueName, new NormalWorker.NormalTask(exchangeName, queueName, message,
								className, methodName, null, tag));
					} else {
						ExecThreadPool.execute(queueName, new NormalWorker.NormalTask(exchangeName, queueName, message,
								className, methodName, channel, tag));
					}

					SkyTrainLogger.log(queueName, "[receive-String]->" + message);
				}
				ClientDataGather.addDeliverDate(queueName);
			}

			@Override
			public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
				LOGGER.error(Const.SIA_LOG_PREFIX + "[网络断开，skytrain尝试重连]", sig);
			}

		};

		try {
			if (exchangeName != null) {
				channel.queueDeclare(queueName, true, false, false, null);
				channel.exchangeDeclare(exchangeName, "fanout", true, false, null);
				channel.queueBind(queueName, exchangeName, "");
			} else {
				channel.queueDeclare(queueName, true, false, false, null);
			}
			channel.basicQos(ExecThreadPool.getPrefetchCount(queueName));
			channel.basicConsume(queueName, autoAck, consumer);
		} catch (IOException ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX, ex);
		}

	}

	/**
	 * ThreadPool Task
	 */
	public static class NormalTask implements Runnable {
		Object message = null;
		String className = null;
		String methodName = null;
		Channel channel = null;
		long consumerTag = -1;
		String exchangeName = null;
		String queueName = null;

		public NormalTask(String exchangeName, String queueName, Object message, String className, String methodName,
				Channel ch, long tag) {
			this.message = message;
			this.className = className;
			this.methodName = methodName;
			this.channel = ch;
			this.consumerTag = tag;
			this.exchangeName = exchangeName;
			this.queueName = queueName;
		}

		/**
		 * run invoke business code
		 */
		@Override
		public void run() {
		    boolean invoked=false;
			try {
				Class<?> clazz;
				try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException ex) {
					clazz = Class.forName(className, true, ClassLoader.getSystemClassLoader());
				}
				Method method;
				// 如果还未创建实例，则创建，并缓存。一致性由 putIfAbsent 保证。
                if (!INSTANCE.containsKey(queueName)) {
                    INSTANCE.putIfAbsent(queueName, clazz.newInstance());
                }
				if (message instanceof SIAMessage) {
					method = clazz.getMethod(methodName, SIAMessage.class);
					method.invoke(INSTANCE.get(queueName), (SIAMessage) message);
					LOGGER.info(Const.SIA_LOG_PREFIX + "[consume-SIAMessage]->" + message);
				} else if (message instanceof String) {
					method = clazz.getMethod(methodName, String.class);
					method.invoke(INSTANCE.get(queueName), (String) message);
					LOGGER.info(Const.SIA_LOG_PREFIX + "[consume-String]->" + message);
				} else {
					method = clazz.getMethod(methodName, Object.class);
					method.invoke(clazz.newInstance(), message);
					LOGGER.error(Const.SIA_LOG_PREFIX + "[never consume]->" + message
							+ " [this is a bug should be reported]");
				}
				invoked=true;
			} catch (ClassNotFoundException ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX + "包类名：[" + className + "]，方法名：[" + methodName + "]，\n交换机名：["
						+ ((exchangeName != null) ? exchangeName : "无") + "]，队列名：[" + queueName + "]，\n消息内容：[" + message
						+ "]" + "\n[接收类错误：请检查配置文件，包名类名是否一致]", ex);
			} catch (NoSuchMethodException ex) {
				LOGGER.error(
						Const.SIA_LOG_PREFIX + "包类名：[" + className + "]，方法名：[" + methodName + "]，\n交换机名：["
								+ ((exchangeName != null) ? exchangeName : "无") + "]，队列名：[" + queueName + "]，\n消息内容：["
								+ message + "]" + "\n[接收方法错误：1，接收参数只能是单个SIAMessage或String类型；2，请检查配置文件，方法名(参数)是否一致]",
						ex);
			} catch (Exception ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX + "包类名：[" + className + "]，方法名：[" + methodName + "]，\n交换机名：["
						+ ((exchangeName != null) ? exchangeName : "无") + "]，队列名：[" + queueName + "]，\n消息内容：[" + message
						+ "]", ex);
			} finally {
			    AckHandler.handleAck(channel, queueName, consumerTag, invoked);
			}
		}
	}

}
