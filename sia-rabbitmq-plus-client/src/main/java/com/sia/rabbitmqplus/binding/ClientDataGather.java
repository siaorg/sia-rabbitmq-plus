package com.sia.rabbitmqplus.binding;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.sia.rabbitmqplus.pojo.Queue;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author xinliang
 * @date 16/11/11.
 */
public class ClientDataGather {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDataGather.class);
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final ThreadLocal<DateFormat> THREAD_LOCAL = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DATE_FORMAT);
		}
	};

	public static Date parse(String dateStr) throws ParseException {
		return THREAD_LOCAL.get().parse(dateStr);
	}

	public static String format(Date date) {
		return THREAD_LOCAL.get().format(date);
	}

	private static final Map<String, Date> PUBLISH_QUEUE = new ConcurrentHashMap<String, Date>();
	private static final Map<String, Date> DELIVER_QUEUE = new ConcurrentHashMap<String, Date>();

	public static void addPublishDate(String queueName) {
		PUBLISH_QUEUE.put(queueName, new Date());
	}

	public static void addDeliverDate(String queueName) {
		DELIVER_QUEUE.put(queueName, new Date());
	}

	private static final Map<String, Map<String, Integer>> DELIVER_QUEUE_OBJECT = new ConcurrentHashMap<String, Map<String, Integer>>();
	private static final String CLIENT_LISTEN_QUEUE = "SKYTRAIN_CLIENT_LISTEN_QUEUE";
	private static final String RECEIVE_QUEUE_FILE = "receivequeue.properties";

	private ClientDataGather() {

	}

	/**
	 * startClientDataGather
	 */
	@SuppressWarnings("AlibabaThreadPoolCreation")
	public static void startClientDataGather() {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				clientDataGatherHandler();
			}
		}, 10L, 600L, TimeUnit.SECONDS);
	}

	/**
	 * clientDataGatherHandler
	 */
	private static void clientDataGatherHandler() {
		try {
			sendPublishInfo();
			sendDeliverInfo();
		} catch (Exception e) {
			LOGGER.error(Const.SIA_LOG_PREFIX, e);
		}
	}

	private static void sendPublishInfo() {
		Channel channel = ChannelPool.open();
		for (String queuename : PUBLISH_QUEUE.keySet()) {
			try {
				Queue queue = new Queue();
				queue.setQueueName(queuename);
				queue.setType("publish");
				queue.setPublishIp(Initial.localIpAddress);
				queue.setPublishRecentTime(format(PUBLISH_QUEUE.get(queuename)));
				queue.setEmailReceviers(Initial.emailReceviers);
				String message = String.valueOf(JSONSerializer.toJSON(queue));
				channel.queueDeclare(CLIENT_LISTEN_QUEUE, true, false, false, null);
				channel.basicPublish("", CLIENT_LISTEN_QUEUE, MessageProperties.PERSISTENT_TEXT_PLAIN,
						message.getBytes("UTF-8"));
			} catch (IOException ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX, ex);
				try {
					Thread.sleep(10 * 1000L);
				} catch (InterruptedException e) {
					LOGGER.error(Const.SIA_LOG_PREFIX + "[等待断线重连]", ex);
				}
			} catch (Exception ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX, ex);
			}
		}
		ChannelPool.close(channel);
	}

	private static void sendDeliverInfo() {
		Channel channel = ChannelPool.open();
		for (String queuename : DELIVER_QUEUE.keySet()) {
			try {
				Queue queue = new Queue();
				queue.setQueueName(queuename);
				queue.setType("deliver");
				queue.setDeliverIp(Initial.localIpAddress);
				queue.setDeliverRecentTime(format(DELIVER_QUEUE.get(queuename)));
				queue.setProjectName(Initial.projectName);
				queue.setProjectDescription(Initial.projectDescription);
				queue.setEmailReceviers(Initial.emailReceviers);
				setAlarmParameters();
				if (DELIVER_QUEUE_OBJECT.containsKey(queuename)) {
					queue.setUnConsumeMessageAlarmNum(
							DELIVER_QUEUE_OBJECT.get(queuename).get("unConsumeMessageAlarmNum"));
					queue.setUnConsumeMessageAlarmGrowthTimes(
							DELIVER_QUEUE_OBJECT.get(queuename).get("unConsumeMessageAlarmGrowthTimes"));
				}
				String message = String.valueOf(JSONSerializer.toJSON(queue));
				channel.queueDeclare(CLIENT_LISTEN_QUEUE, true, false, false, null);
				channel.basicPublish("", CLIENT_LISTEN_QUEUE, MessageProperties.PERSISTENT_TEXT_PLAIN,
						message.getBytes("UTF-8"));
			} catch (IOException ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX, ex);
				try {
					Thread.sleep(10 * 1000L);
				} catch (InterruptedException e) {
					LOGGER.error(Const.SIA_LOG_PREFIX + "[等待断线重连]", ex);

				}
			} catch (Exception ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX, ex);
			}
		}
		ChannelPool.close(channel);
	}

	private static void setAlarmParameters() {
		Properties pro = PropertyHelper.load(RECEIVE_QUEUE_FILE);
		Enumeration<?> en = pro.propertyNames();
		while (en.hasMoreElements()) {
			String exchangeAndQueueName = (String) en.nextElement();
			String queueName = Consumer.getQueueName(exchangeAndQueueName);
			if (!Consumer.checkQueueName(queueName)) {
				continue;
			}
			String jsonStr = pro.getProperty(exchangeAndQueueName);
			JSONObject jsonObject = JSONObject.fromObject(jsonStr);

			Map<String, Integer> queueInfoMap = new HashMap<String, Integer>(2);
			if (jsonObject.containsKey("unConsumeMessageAlarmNum")) {
				queueInfoMap.put("unConsumeMessageAlarmNum", jsonObject.getInt("unConsumeMessageAlarmNum"));
			} else {
				queueInfoMap.put("unConsumeMessageAlarmNum", 100);
			}
			if (jsonObject.containsKey("unConsumeMessageAlarmGrowthTimes")) {
				queueInfoMap.put("unConsumeMessageAlarmGrowthTimes",
						jsonObject.getInt("unConsumeMessageAlarmGrowthTimes"));
			} else {
				queueInfoMap.put("unConsumeMessageAlarmGrowthTimes", 5);
			}
			ClientDataGather.DELIVER_QUEUE_OBJECT.put(queueName, queueInfoMap);
		}
	}

}
