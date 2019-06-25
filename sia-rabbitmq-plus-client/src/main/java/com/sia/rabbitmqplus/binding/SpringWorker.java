package com.sia.rabbitmqplus.binding;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Consumer;
import com.sia.rabbitmqplus.business.ExecThreadPool;
import com.sia.rabbitmqplus.log.SkyTrainLogger;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;


/**
 * Created by xinliang on 16/8/12.
 */
public class SpringWorker implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringWorker.class);

	private String exchangeName;
	private String queueName;
	private String beanName;
	private String methodName;
	private ApplicationContext applicationContext;
	private boolean autoAck;

	/**
	 * structure
	 *
	 * @param exchangeName
	 * @param queueName
	 * @param beanName
	 * @param methodName
	 */
	public SpringWorker(String exchangeName, String queueName, String beanName, String methodName,
			ApplicationContext applicationContext, boolean autoAck) {
		this.exchangeName = exchangeName;
		this.queueName = queueName;
		this.beanName = beanName;
		this.methodName = methodName;
		this.applicationContext = applicationContext;
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
			    //ConsumerBarrier.checkBarrier();
				byte[] bytes = body;
				long tag = envelope.getDeliveryTag();
				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					ObjectInputStream ois = new ObjectInputStream(bais);
					Object message = ois.readObject();
					if (autoAck) {
						ExecThreadPool.execute(queueName, new SpringWorker.SpringTask(exchangeName, queueName, message,
								beanName, applicationContext, methodName, null, tag));
					} else {
						ExecThreadPool.execute(queueName, new SpringWorker.SpringTask(exchangeName, queueName, message,
								beanName, applicationContext, methodName, channel, tag));
					}
					SkyTrainLogger.log(queueName, "[receive-SIAMessage]->" + message);
				} catch (java.lang.ClassNotFoundException ex) {
					LOGGER.error(Const.SIA_LOG_PREFIX, ex);
				} catch (java.io.IOException ex) {
					String message = new String(bytes, Charset.forName("UTF-8"));
					LOGGER.info(Const.SIA_LOG_PREFIX + "非SIAMessage对象，转化为String对象:" + message);
					if (autoAck) {
						ExecThreadPool.execute(queueName, new SpringWorker.SpringTask(exchangeName, queueName, message,
								beanName, applicationContext, methodName, null, tag));
					} else {
						ExecThreadPool.execute(queueName, new SpringWorker.SpringTask(exchangeName, queueName, message,
								beanName, applicationContext, methodName, channel, tag));
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
	 * Spring ThreadPool Task
	 */
	public static class SpringTask implements Runnable {
		Object message = null;
		String beanName = null;
		ApplicationContext applicationContext = null;
		String methodName = null;
		Channel channel = null;
		long consumerTag = -1L;
		String exchangeName = null;
		String queueName = null;

		public SpringTask(String exchangeName, String queueName, Object message, String beanName,
				ApplicationContext applicationContext, String methodName, Channel ch, long tag) {
			this.message = message;
			this.beanName = beanName;
			this.applicationContext = applicationContext;
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
				Object bean = null;
				if (applicationContext != null) {
					bean = applicationContext.getBean(beanName);
				} else {
					LOGGER.error(Const.SIA_LOG_PREFIX + "[applicationContext is null]");
				}
				Method method;
				if (message instanceof SIAMessage) {
					method = bean.getClass().getMethod(methodName, SIAMessage.class);
					method.invoke(bean, (SIAMessage) message);
					LOGGER.info(Const.SIA_LOG_PREFIX + "[consume-SIAMessage]->" + message);
				} else if (message instanceof String) {
					method = bean.getClass().getMethod(methodName, String.class);
					method.invoke(bean, (String) message);
					LOGGER.info(Const.SIA_LOG_PREFIX + "[consume-String]->" + message);
				} else {
					method = bean.getClass().getMethod(methodName, Object.class);
					method.invoke(bean, message);
					LOGGER.error(Const.SIA_LOG_PREFIX + "[never consume]->" + message
							+ " [this is a bug should be reported]");
				}
				invoked=true;
			} catch (BeansException ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX + "bean名：[" + beanName + "]，方法名：[" + methodName + "]，\n交换机名：["
						+ ((exchangeName != null) ? exchangeName : "无") + "]，队列名：[" + queueName + "]，\n消息内容：[" + message
						+ "]" + "\n[接收bean错误：请检查配置文件，bean配置是否正确]", ex);
			} catch (NoSuchMethodException ex) {
				LOGGER.error(
						Const.SIA_LOG_PREFIX + "bean名：[" + beanName + "]，方法名：[" + methodName + "]，\n交换机名：["
								+ ((exchangeName != null) ? exchangeName : "无") + "]，队列名：[" + queueName + "]，\n消息内容：["
								+ message + "]" + "\n[接收方法错误：1，接收参数只能是单个SIAMessage或String类型；2，请检查配置文件，方法名(参数)是否一致]",
						ex);
			} catch (Exception ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX + "bean名 ：[" + beanName + "]，方法名：[" + methodName + "]，\n交换机名：["
						+ ((exchangeName != null) ? exchangeName : "无") + "]，队列名：[" + queueName + "]，\n消息内容：[" + message
						+ "]", ex);
			} finally {
			    AckHandler.handleAck(channel, queueName, consumerTag, invoked);
			}
		}
	}

}
