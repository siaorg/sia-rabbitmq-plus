package com.sia.rabbitmqplus.heartbeat.listener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sia.rabbitmqplus.common.constant.QueueType;
import com.sia.rabbitmqplus.common.helpers.DateFormatHelper;
import com.sia.rabbitmqplus.common.helpers.JSONHelper;
import com.sia.rabbitmqplus.common.pojo.Queue;
import com.sia.rabbitmqplus.common.pojo.QueueInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author xinliang
 * @date 16/11/11.
 */
@Component
@RabbitListener(queues = "SKYTRAIN_CLIENT_LISTEN_QUEUE")
public class ClientDataListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDataListener.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 监听客户端发送的信息进行处理
     *
     * @param object
     */
    @RabbitHandler
    private void clientInfoHandler(Object object) {

        try {
            Message message = (Message) object;
            String info = new String(message.getBody(), "UTF-8");
           
            Queue data = JSONHelper.toObject(info, Queue.class);
            String queueName = data.getQueueName();
            
            String type = data.getType();
            if (QueueType.DELIVER.equals(type)) {
                updateDeliver(data, queueName);
            }
            else if (QueueType.PUBLISH.equals(type)) {
                updatePublish(data, queueName);
            }
            else {
                LOGGER.error("[THIS IS A BUG]");
            }
        }
        catch (Exception ex) {
            LOGGER.error("", ex);
        }
    }

    private static void updateEmails(QueueInfo nowQueueInfo, String[] emailReceviers, boolean append) {

        List<String> tempEmails = new ArrayList<String>(
                Arrays.asList(nowQueueInfo.getProjectInfo().getEmailReceviers()));
        if (append && tempEmails != null && tempEmails.size() > 0) {
            for (String email : emailReceviers) {
                if (!tempEmails.contains(email)) {
                    tempEmails.add(email);
                }
            }
            nowQueueInfo.getProjectInfo().setEmailReceviers(tempEmails.toArray(new String[tempEmails.size()]));
            return;
        }
        nowQueueInfo.getProjectInfo().setEmailReceviers(emailReceviers);

    }

    private static boolean isEmpty(String str) {

        return str == null || str.length() == 0;
    }

    private void updatePublish(Queue data, String queueName) {

        String publishIp = data.getPublishIp();
        String publishRecentTime = data.getPublishRecentTime();
        String[] emailReceviers = data.getEmailReceviers().split(",");

        try {

            String queueInfoObject = redisTemplate.opsForValue().get(queueName);
            /**
             * 这里加不加锁已经没意义了。这个应用是单线程操作，不需加锁。不同的应用不在同一个JVM中，锁对象又不是分布式的，加了也不起作用。
             * <p>
             * 由于并发读写，可能读到错误的数据。这里直接新建一个对象。
             */
            QueueInfo nowQueueInfo = null;

            if (queueInfoObject == null) {
                nowQueueInfo = new QueueInfo();
            }
            else {
                try {
                    nowQueueInfo = JSONHelper.toObject(queueInfoObject, QueueInfo.class);
                }
                catch (Exception e) {
                    nowQueueInfo = new QueueInfo();
                }
            }
            nowQueueInfo.setQueueName(queueName);
            
            // end of modify
            Map<String, String> publishIps = nowQueueInfo.getPublishIps();
            if (publishIps == null) {
                publishIps = new TreeMap<String, String>();
            }
            publishIps = cleanIP(publishIps);
            publishIps.put(publishIp, publishRecentTime);
            nowQueueInfo.setPublishIps(publishIps);

            updateEmails(nowQueueInfo, emailReceviers, true);

            redisTemplate.opsForValue().set(queueName, JSONHelper.toString(nowQueueInfo));
        }
        catch (Exception ex) {
            LOGGER.error("", ex);
        }

    }

    private void updateDeliver(Queue data, String queueName) {

        String projectName = data.getProjectName();
        String projectDescription = data.getProjectDescription();
        String[] emailReceviers = data.getEmailReceviers().split(",");
        String deliverIp = data.getDeliverIp();
        String deliverRecentTime = data.getDeliverRecentTime();
        int unConsumeMessageAlarmNum = data.getUnConsumeMessageAlarmNum();
        int unConsumeMessageAlarmGrowthTimes = data.getUnConsumeMessageAlarmGrowthTimes();

        try {
            String queueInfoObject = redisTemplate.opsForValue().get(queueName);
            /**
             * 这里加不加锁已经没意义了。这个应用是单线程操作，不需加锁。不同的应用不在同一个JVM中，锁对象又不是分布式的，加了也不起作用。
             * <p>
             * 由于并发读写，可能读到错误的数据。这里直接新建一个对象。
             */
            QueueInfo nowQueueInfo = null;

            if (queueInfoObject == null) {
                nowQueueInfo = new QueueInfo();
            }
            else {
                try {
                    nowQueueInfo = JSONHelper.toObject(queueInfoObject, QueueInfo.class);
                }
                catch (Exception e) {
                    nowQueueInfo = new QueueInfo();
                }
            }
            nowQueueInfo.setQueueName(queueName);
            
            // end of modify
            Map<String, String> deliverIps = nowQueueInfo.getDeliverIps();
            if (deliverIps == null) {
                deliverIps = new TreeMap<String, String>();
            }
            deliverIps = cleanIP(deliverIps);
            deliverIps.put(deliverIp, deliverRecentTime);
            nowQueueInfo.setDeliverIps(deliverIps);
            nowQueueInfo.getProjectInfo().setProjectName(projectName);
            nowQueueInfo.getProjectInfo().setProjectDescription(projectDescription);

            updateEmails(nowQueueInfo, emailReceviers, false);

            nowQueueInfo.setUnConsumeMessageAlarmNum(unConsumeMessageAlarmNum);
            nowQueueInfo.setUnConsumeMessageAlarmGrowthTimes(unConsumeMessageAlarmGrowthTimes);
            redisTemplate.opsForValue().set(queueName, JSONHelper.toString(nowQueueInfo));
        }
        catch (Exception ex) {
            LOGGER.error("", ex);
        }

    }

    private final long MAX_INTERVAL_MILLIS = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 因为某些（很多）项目组使用容器，抓取的发送接收的IP一直在变，也一直在累积，怎么解决？
     * <p>
     * 因为历史记录只保留7天，对于上传的IP信息来说 publishIps = new TreeMap<String, String>()
     * <p>
     * （1）清除7天之前的信息
     * <p>
     * （2）清除对于IP为空的信息
     */
    private Map<String, String> cleanIP(Map<String, String> dirtyIP) {

        Map<String, String> resp = new TreeMap<String, String>();
        if (dirtyIP == null || dirtyIP.isEmpty()) {
            return resp;
        }
        final long current = System.currentTimeMillis();
        for (Map.Entry<String, String> item : dirtyIP.entrySet()) {
            String key = item.getKey();
            String value = item.getValue();
            if (isEmpty(key) || isEmpty(value)) {
                continue;
            }

            try {
                Date date = DateFormatHelper.parse(value);
                long last = date.getTime();
                if (current - last < MAX_INTERVAL_MILLIS) {
                    resp.put(key, value);
                }
            }
            catch (ParseException e) {
                // ignore
            }
        }
        return resp;
    }

}
