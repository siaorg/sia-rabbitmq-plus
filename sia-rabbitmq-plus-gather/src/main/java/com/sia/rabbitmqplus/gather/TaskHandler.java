package com.sia.rabbitmqplus.gather;

import com.sia.rabbitmqplus.gather.database.DataBaseHandler;
import com.sia.rabbitmqplus.gather.email.EmailWorker;
import com.sia.rabbitmqplus.gather.pojo.QueueInfo;
import com.sia.rabbitmqplus.helpers.JSONHelper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * Created by xinliang on 2017/10/26.
 */
@RestController
public class TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskHandler.class);

    @Value("${SKYTRAIN_RABBITMQ_HTTP_USERNAME}")
    protected String rabbitmqHttpUserName;

    @Value("${SKYTRAIN_RABBITMQ_HTTP_PASSWORD}")
    protected String rabbitmqHttpPassword;

    @Autowired
    protected DataBaseHandler DBHandler;

    @Autowired
    protected EmailWorker emailWorker;

    @Value("${ENABLE_EMAIL_ALARM}")
    protected boolean enableEmailAlarm;

    @Autowired
    @Qualifier("redisTemplate")
    public StringRedisTemplate redisTemplate;

    // 定时模块。表示从10s开始，每隔1分钟执行一次
    @Scheduled(initialDelay = 10000L, fixedRate = 60000L)
    @RequestMapping(value = "/statistics", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
    @ResponseBody
    public String scheduledTaskHandler() {

        // 全局变量，用来记录调用的状态(status和result)
        Map<String, String> information = new HashMap<String, String>(16);
        // 当前时间 begin
        long begin = System.currentTimeMillis();
        try {
            // info是查询队列的信息 密码和名字
            String info = queryRabbitmqStringInfo();
            LOGGER.info(" queryRabbitmqStringInfo cost: " + (System.currentTimeMillis() - begin) + " Millis");
            @SuppressWarnings("rawtypes")
            List<Map> queues = JSONHelper.toObjectArray(info, Map.class);
            // 队列信息
            QueueInfo queueInfo;
            // 队列名字
            String queueName;
            // 消息条数
            Integer messages;
            // 消费者个数
            Integer consumers;
            Integer publishTemp;
            Long publish;
            Integer deliverTemp;
            Long deliver;
            // 生产的消息个数
            Long publishMessageNum;
            // 消费的消息个数
            Long deliverMessageNum;
            // 消息统计数据
            Map<?, ?> messageStatsMap;
            int lastIncTimes = 0;
            List<QueueInfo> dbQueueInfos = new ArrayList<QueueInfo>();

            Integer publishTotal = new Integer(0);
            Integer deliverTotal = new Integer(0);

            int tmpQueues = 0;
            int hasNoConsumers = 0;
            // 遍历List集合，List集合中又是一个Map
            for (Map<?, ?> queue : queues) {

                publish = new Long(0);
                deliver = new Long(0);

                Map<?, ?> temporary = (Map<?, ?>) queue.get("arguments");
                if (!temporary.isEmpty()) {
                    tmpQueues++;
                    continue;
                }

                // TODO:过滤一些自动删除的队列，标识为AD，关键属性为durable，auto_delete
                // boolean durable = (Boolean) queue.get("durable");
                // if (!durable) {
                // tmpQueues++;
                // continue;
                // }
                // boolean auto_delete = (Boolean) queue.get("auto_delete");
                // if (auto_delete) {
                // tmpQueues++;
                // continue;
                // }
                // end，这段代码主要是过滤SpringCloudBus建立的owner的AD队列
                queueName = (String) queue.get("name");
                if (queueName.startsWith("amq.gen")) {
                    continue;
                }
                // 这里可以看出map里面装得是message和消费者的个数
                messages = (Integer) queue.get("messages");
                consumers = (Integer) queue.get("consumers");

                if (consumers <= 0) {
                    hasNoConsumers++;
                }

                messageStatsMap = (Map<?, ?>) queue.get("message_stats");
                if (messageStatsMap != null) {
                    publishTemp = (Integer) messageStatsMap.get("publish");
                    publish = Long.valueOf(publishTemp == null ? new Long(0) : publishTemp);
                    deliverTemp = (Integer) messageStatsMap.get("deliver_get");
                    deliver = Long.valueOf(deliverTemp == null ? new Long(0) : deliverTemp);
                }
                // query from redis and transfer pojo

                try {

                    String queueInfoObject = redisTemplate.opsForValue().get(queueName);
                    /**
                     * 
                     * 由于并发读写，可能读到错误的数据。这里直接新建一个对象。
                     */
                    if (queueInfoObject == null) {
                        queueInfo = new QueueInfo();
                    }
                    else {
                        try {
                            queueInfo = JSONHelper.toObject(queueInfoObject, QueueInfo.class);
                        }
                        catch (Exception e) {
                            queueInfo = new QueueInfo();
                        }
                    }
                    // end of modify
                    // publish 生产的消息数目 getAllPublishMessageNum历史发送消息数目
                    publishMessageNum = publish - queueInfo.getAllPublishMessageNum();
                    queueInfo.setPublishMessageNum(publishMessageNum.intValue());

                    publishTotal += publishMessageNum.intValue();
                    // deliver
                    deliverMessageNum = deliver - queueInfo.getAllDeliverMessageNum();
                    queueInfo.setDeliverMessageNum(deliverMessageNum.intValue());

                    deliverTotal += deliverMessageNum.intValue();
                    // incTimes
                    if (messages > queueInfo.getUnConsumeMessageNum()) {
                        lastIncTimes = queueInfo.getUnConsumeMessageGrowthTimes();
                        queueInfo.setUnConsumeMessageGrowthTimes(lastIncTimes + 1);
                    }
                    else {
                        queueInfo.setUnConsumeMessageGrowthTimes(0);
                    }

                    queueInfo.setQueueName(queueName);
                    queueInfo.setUnConsumeMessageNum(messages);
                    queueInfo.setQueueConsumers(consumers);
                    queueInfo.setAllPublishMessageNum(publish);
                    queueInfo.setAllDeliverMessageNum(deliver);
                    // add info
                    dbQueueInfos.add(queueInfo);
                    // alarm
                    if (enableEmailAlarm) {
                        emailWorker.alarm(queueInfo);
                    }
                    // transfer json and save to redis
                    redisTemplate.opsForValue().set(queueName, JSONHelper.toString(queueInfo), 10, TimeUnit.MINUTES);
                }
                catch (Exception ex) {
                    LOGGER.error("queueName:[" + queueName + "]", ex);
                    information.put("status", "failure");
                    information.put("result", "Redis Exception");
                }

            }
            LOGGER.info(" RabbitMQ has " + tmpQueues + " temporary queues");
            LOGGER.info(" RabbitMQ has " + queues.size() + " queues at this moment");
            LOGGER.info(" RabbitMQ has " + hasNoConsumers + " queues with no consumer");
            // put history to MySql or SystemProperties
            long start = System.currentTimeMillis();
            DBHandler.insertHistoryBatch(dbQueueInfos);
            LOGGER.info(" insertHistoryBatch cost: " + (System.currentTimeMillis() - start) + " Millis");
            information.put("status", "success");
            information.put("result", "success");
        }
        catch (FeignException ex) {
            emailWorker.sendSOS(ex);
            information.put("status", "failure");
            information.put("result", "Feign Exception");
            LOGGER.error("", ex);
        }
        catch (Exception ex) {
            LOGGER.error("", ex);
            information.put("status", "failure");
            information.put("result", "Other Exception");
        }
        LOGGER.info(" insertOrUpdateQueueInfoFromRabbitmqToRedis cost: " + (System.currentTimeMillis() - begin)
                + " Millis");
        return JSONHelper.toString(information);
    }

    // 注入服务提供者,远程的Http服务
    @Autowired
    private TaskService taskService;

    /**
     * queryRabbitmqStringInfo 将队列信息绑定在http的请求头上
     * 
     * @return
     */
    private String queryRabbitmqStringInfo() {

        String nameAndPass = rabbitmqHttpUserName + ":" + rabbitmqHttpPassword;
        byte[] nameAndPassbyte = nameAndPass.getBytes(Charset.forName("UTF-8"));
        byte[] credentials = encodeBase64(nameAndPassbyte);
        String token = "Basic " + new String(credentials, Charset.forName("UTF-8"));
        return taskService.query(token);
    }

    private static final long SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000L;

    //@OnlineTask(description = "定时清理历史数据", enableSerial = true)
    @RequestMapping(value = "/clean", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
    @ResponseBody
    public String scheduledClearHistory() {

        // 当前时间 begin
        long begin = System.currentTimeMillis();
        Map<String, String> response = new HashMap<String, String>(16);
        DBHandler.clearHistory(SEVEN_DAYS);
        response.put("status", "success");
        response.put("result", "success");
        LOGGER.info(" scheduledClearHistory cost: " + (System.currentTimeMillis() - begin) + " Millis");
        return JSONHelper.toString(response);
    }

}
