package com.sia.rabbitmqplus.binding;

import com.rabbitmq.client.Channel;
import com.sia.rabbitmqplus.business.ExecThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author: pengfeili23@creditease.cn
 * @Description: 处理消费端消息ACK的逻辑
 * @date: 2019年3月26日 下午3:51:06
 *
 */
public class AckHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AckHandler.class);

    /**
     * 令牌桶算法，用作限流，预防[消息处理失败->重回队列->消息处理失败->重回队列]死循环发生
     */
    private static final RateLimiter LIMITER = new RateLimiter(TimeUnit.MINUTES);

    /**
     * 如果是自动回复ACK，则传入的 channel 为 null，此时不需处理。
     * <p>
     * 如果是手动处理ACK，则传入的 channel 不为 null，根据业务调用结果处理。
     * <p>
     * 如果业务调用成功，则回复ACK； 如果业务调用出异常，则按照模式处理。
     * <p>
     * ACK-服务器清除消息；NACK-消息重回队列
     * 
     * @param channel
     *            与消费端队列绑定的连接通道
     * @param queueName
     *            消费端队列
     * @param deliveryTag
     *            消息编号，唯一定位消息
     * @param invoked
     *            业务调用是否成功
     */
    public static void handleAck(Channel channel, String queueName, long deliveryTag, boolean invoked) {

        /**
         * 如果是自动回复ACK，则传入的 channel 为 null，此时不需处理。
         */
        if (channel == null) {
            return;
        }
        /**
         * 如果业务调用成功，则回复ACK
         */
        if (invoked) {
            handleSuccess(channel, deliveryTag);
            return;
        }
        /**
         * 如果业务调用出异常，则按照模式处理
         */
        String ackModel = ExecThreadPool.getAckModel(queueName);
        if (ackModel == null) {
            ackModel = Const.DEFAULT;
        }
        switch (ackModel) {
            /**
             * ACK-服务器清除消息
             */
            case Const.ACK:
                handleACK(channel, deliveryTag);
                LOGGER.info(Const.SIA_LOG_PREFIX + "ackModel:[ACK], queueName:[" + queueName + "], deliveryTag:["
                        + deliveryTag + "]");
                break;
            /**
             * NACK-消息重回队列
             */
            case Const.NACK:
                int size = ExecThreadPool.getPrefetchCount(queueName);
                handleNACK(channel, deliveryTag, size);
                LOGGER.info(Const.SIA_LOG_PREFIX + "ackModel:[NACK], queueName:[" + queueName + "], deliveryTag:["
                        + deliveryTag + "]");
                break;
            /**
             * 默认回复ACK-服务器清除消息
             */
            default:
                LOGGER.info(Const.SIA_LOG_PREFIX + "ackModel:[DEFAULT-ACK], queueName:[" + queueName + "], deliveryTag:["
                        + deliveryTag + "]");
                handleACK(channel, deliveryTag);
        }

    }

    private static void handleSuccess(Channel channel, long deliveryTag) {

        handleACK(channel, deliveryTag);
    }

    private static void handleACK(Channel channel, long deliveryTag) {

        try {
            channel.basicAck(deliveryTag, false);
            LOGGER.info(Const.SIA_LOG_PREFIX + "[手动消息确认ACK成功：deliveryTag]->" + deliveryTag);
        }
        catch (IOException e) {
            LOGGER.error(Const.SIA_LOG_PREFIX + "[手动消息确认ACK失败：deliveryTag]->" + deliveryTag, e);
        }
    }

    private static void handleNACK(Channel channel, long deliveryTag, int size) {

        try {
            if (LIMITER.acquire(size, size)) {
                channel.basicNack(deliveryTag, false, true);
                LOGGER.info(Const.SIA_LOG_PREFIX + "[消息重回队列][手动消息确认NACK成功：deliveryTag]->" + deliveryTag);
            }
            else {
                channel.basicNack(deliveryTag, false, false);
                LOGGER.info(Const.SIA_LOG_PREFIX + "[NACK重复多次，消息不回队列][手动消息确认NACK成功：deliveryTag]->" + deliveryTag);
            }

        }
        catch (IOException e) {
            LOGGER.error(Const.SIA_LOG_PREFIX + "[手动消息确认NACK失败：deliveryTag]->" + deliveryTag, e);
        }
    }

}
