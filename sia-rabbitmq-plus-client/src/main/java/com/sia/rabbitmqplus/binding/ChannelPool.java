package com.sia.rabbitmqplus.binding;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinliang
 * @date 16/8/3
 */
public class ChannelPool {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);
	private static LinkedBlockingQueue<Channel> channelQueue = null;
	private static Connection connection = null;
	private static final int CONNECTION_LIMIT_SIZE = 60;

	private ChannelPool() {

	}

	/**
	 * init
	 */
	protected static boolean init(Properties prop) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(prop.getProperty("RABBITMQ_HOST").trim());
		factory.setPort(Integer.parseInt(prop.getProperty("RABBIT_PORT").trim()));
		factory.setUsername(prop.getProperty("RABBITMQ_USERNAME"));
		factory.setPassword(prop.getProperty("RABBITMQ_PASSWORD"));
		factory.setAutomaticRecoveryEnabled(true);
		factory.setHandshakeTimeout(60 * 1000);
		LOGGER.info(Const.SIA_LOG_PREFIX + "[RABBIT_HOST:<" + Const.RABBIT_HOST + ">]");
		LOGGER.info(Const.SIA_LOG_PREFIX + "[RABBIT_PORT:<" + Const.RABBIT_PORT + ">]");

		try {
			connection = factory.newConnection(getListAddress());
		} catch (IOException e) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "[连接RabbitMQ服务器出错，请检查网络是否正常，<serverIP>与<port>的设置是否正确？]", e);
			return false;

		} catch (TimeoutException e) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "[连接RabbitMQ服务器超时，请检查网络是否正常，<serverIP>与<port>的设置是否正确？]", e);
			return false;

		}
		LOGGER.info(Const.SIA_LOG_PREFIX + "[连接RabbitMQ服务器成功，<serverIP>:" + Const.RABBIT_HOST + ",<port>:"
				+ Const.RABBIT_PORT + "]");
		channelQueue = new LinkedBlockingQueue<Channel>(Const.CHANNELPOOL_QUEUE_SIZE);
		return true;
	}

	/**
	 * init
	 */
	static {
		Initial.init();
	}

	/**
	 * 从池中获取一个通道
	 *
	 * @return channel
	 */
	public static Channel open() {
		Channel channel = null;
		try {
			if (channelQueue != null) {
				channel = channelQueue.poll();
			}
			if (channel == null || !channel.isOpen()) {
				if (null == connection) {
					Initial.await();
					if (false == Initial.isReady()) {
						LOGGER.error(Const.SIA_LOG_PREFIX + "[未成功连接队列服务器]");
						throw new IllegalStateException("[未成功连接队列服务器]");
					}
				}
				for (int i = 0; i < CONNECTION_LIMIT_SIZE && !connection.isOpen(); i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOGGER.error(Const.SIA_LOG_PREFIX, e);
					}
					LOGGER.error(Const.SIA_LOG_PREFIX + "[网络断开，skytrain尝试重连][" + Thread.currentThread().getName()
							+ "]等待时间(单位：秒)：" + i);
				}
				channel = connection.createChannel();
			}
		} catch (IOException ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX, ex);
		}
		return channel;
	}

	/**
	 * 将通道放回池中
	 *
	 * @param channel
	 */
	public static void close(Channel channel) {
		if (channelQueue == null || !channelQueue.offer(channel)) {
			try {
				if (channel.isOpen()) {
					channel.close();
				}
			} catch (IOException ex) {
				LOGGER.error(Const.SIA_LOG_PREFIX, ex);
			} catch (TimeoutException e) {
				LOGGER.error(Const.SIA_LOG_PREFIX, e);
			}
		}
	}

	/**
	 * close connection
	 */
	public static void close() {
		try {
			if (connection.isOpen()) {
				connection.close();
			}
		} catch (IOException ex) {
			LOGGER.error(Const.SIA_LOG_PREFIX, ex);
		}
	}
	
	/**
	 * MQ多地址连接
	 * @return
	 */
	private static List<Address> getListAddress() {

        List<Address> addrs = new LinkedList<Address>();
        
        String[] hosts= Const.RABBIT_HOST.split(",");
        for(int i=0;i<hosts.length;i++) {
            Address addr=new Address(hosts[i].trim(), Const.RABBIT_PORT);
            addrs.add(addr);
        }
        //FisherYates洗牌算法，打乱地址的次序，用作客户端负载均衡
        Collections.shuffle(addrs);
        return addrs;
    }
}
