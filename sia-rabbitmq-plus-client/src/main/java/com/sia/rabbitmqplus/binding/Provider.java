package com.sia.rabbitmqplus.binding;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Consumer;
import com.sia.rabbitmqplus.log.SkyTrainLogger;
import com.sia.rabbitmqplus.pojo.SIAMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * @author xinliang on 16/8/9.
 */
public class Provider {

    private static final Logger LOGGER = LoggerFactory.getLogger(Provider.class);
    private static final String OBJECT = "object";
    private static final String BEGIN_SEND ="beginSend:";

    /**
     * @param exchangeName
     * @param queueName
     * @param message
     * @throws Exception
     */
    public void send(String exchangeName, String queueName, String message) throws Exception {
        Channel channel = ChannelPool.open();
        try {
            if (exchangeName != null) {
                sendPubSub(channel, exchangeName, message);
            } else if (queueName != null) {
                sendP2P(channel, queueName, message);
            } else {
                throw new IllegalArgumentException("[queueName或exchangeName不能都为空]");
            }
        } finally {
            ChannelPool.close(channel);
        }
    }

    private void sendPubSub(Channel channel, String exchangeName, String message) throws Exception {
        declareExchange(channel,exchangeName,message);
        channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
        SkyTrainLogger.log(exchangeName, "[fanout:send-String]->" + message);
    }

    private void sendP2P(Channel channel, String queueName, String message) throws Exception {
        declareQueue(channel,queueName,message);
        channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
        ClientDataGather.addPublishDate(queueName);
        SkyTrainLogger.log(queueName, "[send-String]->" + message);
    }

    /**
     * @param message
     */
    private void send(SIAMessage message, String lable, boolean isChainModel) throws Exception {
        Channel channel = ChannelPool.open();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);
            String exchangeName = message.getGroupCode();
            String queueName = message.getBusinessCode();
            if (exchangeName != null) {
                declareExchange(channel,exchangeName,message);
                if (OBJECT.equalsIgnoreCase(message.getMessageType())) {
                    channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, baos.toByteArray());
                    SkyTrainLogger.log(exchangeName, "[fanout:send-SIAMessage]->" + message);
                } else {
                    channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN,
                            message.getMessageInfoClob().getBytes("UTF-8"));
                    SkyTrainLogger.log(exchangeName, "[fanout:send-String]->" + message.getMessageInfoClob());
                }
            } else if (queueName != null) {
                if (isChainModel) {
                    channel.queueDeclare(queueName, true, false, false, null);
                }
                if (message.getReceiveQueueName() != null && BEGIN_SEND.equals(lable)) {
                    Map<String, Object> args = new HashMap<String, Object>(1);
                    if (message.getExpires() != null) {
                        // 队列超时时间30s
                        args.put("x-expires", message.getExpires() * 1000);
                    } else {
                        // 队列超时时间30s
                        args.put("x-expires", 30 * 1000);
                    }
                    channel.queueDeclare(message.getReceiveQueueName(), false, false, false, args);
                }
                if (OBJECT.equalsIgnoreCase(message.getMessageType())) {
                    channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, baos.toByteArray());
                    ClientDataGather.addPublishDate(queueName);
                    SkyTrainLogger.log(queueName, "[" + lable + "send-SIAMessage]->" + message);
                } else {
                    channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN,
                            message.getMessageInfoClob().getBytes("UTF-8"));
                    ClientDataGather.addPublishDate(queueName);
                    SkyTrainLogger.log(queueName, "[send-String]->" + message.getMessageInfoClob());
                }
            } else {
                throw new IllegalArgumentException("[businessCode(queueName)或groupCode(exchangeName)不能都为空]");
            }
            baos.close();
            oos.close();
        } catch (Exception ex) {
            throw ex;
        } finally {
            ChannelPool.close(channel);
        }
    }

    public void send(SIAMessage message) throws Exception {
        send(message, "", false);
    }

    public void beginSend(SIAMessage message) throws Exception {
        if (OBJECT.equalsIgnoreCase(message.getMessageType())) {
            send(message, "beginSend:", true);
        } else {
            throw new IllegalArgumentException(
                    "[链条模式下，初始发送必须为SIAMessage，类型必须为Object]，出错消息类型：" + message.getMessageType());
        }
    }

    public void middleSend(SIAMessage message) throws Exception {
        if (OBJECT.equalsIgnoreCase(message.getMessageType())) {
            send(message, "middleSend:", true);
        } else {
            throw new IllegalArgumentException(
                    "[链条模式下，中间发送必须为SIAMessage，类型必须为Object]，出错消息类型：" + message.getMessageType());
        }
    }

    /**
     * 因为是同步链条模式的callback阶段，回复queue早已申明，只能是一对一的回复
     *
     * @param message
     * @throws Exception
     */
    public void endSend(SIAMessage message) throws Exception {
        Channel channel = ChannelPool.open();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);
            String queueName = message.getReceiveQueueName();
            if (queueName != null) {
                try {
                    channel.queueDeclarePassive(queueName);
                } catch (IOException ex) {
                    LOGGER.error(Const.SIA_LOG_PREFIX, ex);
                    SkyTrainLogger.log(queueName, "[由于出错而保存消息：]->" + message);
                    throw new RuntimeException("[链条请求超时！]");
                }
                if (OBJECT.equalsIgnoreCase(message.getMessageType())) {
                    channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, baos.toByteArray());
                    SkyTrainLogger.log("_END_SEND_CALL_BACK_", "[endSend:send-SIAMessage]->" + message);
                } else {
                    channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN,
                            message.getMessageInfoClob().getBytes("UTF-8"));
                    SkyTrainLogger.log("_END_SEND_CALL_BACK_",
                            "[endSend:send-String]->" + message.getMessageInfoClob());
                }
            } else {
                SkyTrainLogger.log(queueName, "[由于出错而保存消息：]->" + message);
                throw new IllegalArgumentException("[同步链条模式下中间调用没有设置（起始调用指定的）回调队列名！]");
            }
            baos.close();
            oos.close();
        } catch (Exception ex) {
            throw ex;
        } finally {
            ChannelPool.close(channel);
        }
    }

    /**
     * @param message
     * @return
     * @throws Exception
     */
    public String receive(SIAMessage message) throws Exception {
        Channel channel = ChannelPool.open();
        final BlockingQueue<String> response = new LinkedBlockingQueue<String>();
        String result = "";
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                byte[] bytes = body;
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    SIAMessage siamessage = (SIAMessage) ois.readObject();
                    response.add(siamessage.getMessageInfoClob());
                    SkyTrainLogger.log("_BEGIN_SEND_RECEIVE_", "[receive-SIAMessage]->" + siamessage);
                } catch (java.lang.ClassNotFoundException ex) {
                    LOGGER.error(Const.SIA_LOG_PREFIX, ex);
                } catch (java.io.IOException ex) {
                    String message = new String(bytes, Charset.forName("UTF-8"));
                    LOGGER.info(Const.SIA_LOG_PREFIX + "非SIAMessage对象，转化为String对象:" + message);
                    response.add(message);
                    SkyTrainLogger.log("_BEGIN_SEND_RECEIVE_", "[receive-String]->" + message);
                }
            }

            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                LOGGER.error(Const.SIA_LOG_PREFIX + "[网络断开，sia尝试重连]", sig);
            }
        };

        try {
            channel.basicConsume(message.getReceiveQueueName(), true, consumer);
            result = response.poll(message.getTimeout(), TimeUnit.SECONDS);
            if (result == null) {
                throw new RuntimeException("[链条请求超时！]");
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            channel.basicCancel(((DefaultConsumer) consumer).getConsumerTag());
            ChannelPool.close(channel);
        }
        return result;
    }

    public void declareExchange(Channel channel, String exchangeName,Object message){
        try {
            channel.exchangeDeclarePassive(exchangeName);
        } catch (IOException ex) {
            LOGGER.error(Const.SIA_LOG_PREFIX, ex);
            SkyTrainLogger.log(exchangeName, "[由于出错而保存消息：]->" + message);
            throw new UnsupportedOperationException(
                    "[rabbitMQ服务器上没有名为<" + exchangeName + ">的交换机，请联系接收端，让其先启动发布订阅模式下的消费者]");
        }
    }

    public void declareQueue(Channel channel, String queueName, String message){
        try {
            channel.queueDeclarePassive(queueName);
        } catch (IOException ex) {
            LOGGER.error(Const.SIA_LOG_PREFIX, ex);
            SkyTrainLogger.log(queueName, "[由于出错而保存消息：]->" + message);
            throw new UnsupportedOperationException(
                    "[rabbitMQ服务器上没有名为<" + queueName + ">队列，请联系接收端，让其先启动点对点模式下的消费者]");
        }
    }
}
