package com.sia.rabbitmqplus.heartbeat.http;
//
//import com.creditease.skytrain.supervise.database.DataOperater;
//import com.creditease.skytrain.supervise.email.EmailWorker;
//import com.creditease.skytrain.supervise.middleware.RedisLock;
//import QueueInfo;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.http.HttpEntity;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.log4j.Logger;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.type.TypeReference;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.locks.Lock;
//
///**
// * @author xinliang on 16/11/11.
// */
//// @Component
//public class RabbitmqHttpWorker {
//
//    private static final Logger LOGGER = Logger.getLogger(RabbitmqHttpWorker.class);
//
//    private static CloseableHttpClient httpclient = null;
//    private static HttpGet httpget = null;
//
//    @Autowired
//    protected DataOperater dataOperater;
//
//    @Autowired
//    @Qualifier("redisTemplate")
//
//    public StringRedisTemplate redisTemplate;
//
//    @Autowired
//    protected EmailWorker emailWorker;
//
//    @Value("${ENABLE_EMAIL_ALARM}")
//    protected boolean enableEmailAlarm;
//
//    /**
//     * queryRabbitmqListInfo
//     *
//     * @return
//     */
//    // @Scheduled(initialDelay = 10000L, fixedRate = 60000L)
//    public void insertOrUpdateQueueInfoFromRabbitmqToRedis() {
//
//        long begin = System.currentTimeMillis();
//        try {
//            String info = queryRabbitmqStringInfo();
//            LOGGER.info(" queryRabbitmqStringInfo cost: " + (System.currentTimeMillis() - begin) + " Millis");
//            ObjectMapper mapper = new ObjectMapper();
//            // JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, Map.class);
//            // List<Map> queues = mapper.readValue(info, javaType);
//            @SuppressWarnings("rawtypes")
//
//            List<Map> queues = mapper.readValue(info, new TypeReference<List<Map>>() {
//            });
//
//            QueueInfo queueInfo;
//            String queueName;
//            Integer messages;
//            Integer consumers;
//            Integer publishTemp;
//            Long publish;
//            Integer deliverTemp;
//            Long deliver;
//            Long publishMessageNum;
//            Long deliverMessageNum;
//            Map<?, ?> messageStatsMap;
//            int lastIncTimes = 0;
//            List<QueueInfo> dbQueueInfos = new ArrayList<QueueInfo>();
//
//            Integer publishTotal = new Integer(0);
//            Integer deliverTotal = new Integer(0);
//            for (Map<?, ?> queue : queues) {
//
//                publish = new Long(0);
//                deliver = new Long(0);
//
//                Map<?, ?> temporary = (Map<?, ?>) queue.get("arguments");
//                if (temporary.isEmpty()) {
//                    queueName = (String) queue.get("name");
//                    messages = (Integer) queue.get("messages");
//                    consumers = (Integer) queue.get("consumers");
//
//                    messageStatsMap = (Map<?, ?>) queue.get("message_stats");
//                    if (messageStatsMap != null) {
//                        publishTemp = (Integer) messageStatsMap.get("publish");
//                        publish = Long.valueOf(publishTemp == null ? new Long(0) : publishTemp);
//                        deliverTemp = (Integer) messageStatsMap.get("deliver_get");
//                        deliver = Long.valueOf(deliverTemp == null ? new Long(0) : deliverTemp);
//                    }
//                    // query from redis and transfer pojo
//                    Lock lock = RedisLock.getLock(queueName);
//                    lock.lock();
//                    try {
//
//                        Object queueInfoObject = redisTemplate.opsForValue().get(queueName);
//                        if (queueInfoObject == null) {
//                            queueInfo = new QueueInfo();
//                        }
//                        else {
//                            queueInfo = mapper.readValue((String) queueInfoObject, QueueInfo.class);
//                        }
//
//                        // publish
//                        publishMessageNum = publish - queueInfo.getAllPublishMessageNum();
//                        queueInfo.setPublishMessageNum(publishMessageNum.intValue());
//
//                        publishTotal += publishMessageNum.intValue();
//                        // deliver
//                        deliverMessageNum = deliver - queueInfo.getAllDeliverMessageNum();
//                        queueInfo.setDeliverMessageNum(deliverMessageNum.intValue());
//
//                        deliverTotal += deliverMessageNum.intValue();
//                        // incTimes
//                        if (messages > queueInfo.getUnConsumeMessageNum()) {
//                            lastIncTimes = queueInfo.getUnConsumeMessageGrowthTimes();
//                            queueInfo.setUnConsumeMessageGrowthTimes(lastIncTimes + 1);
//
//                        }
//                        else {
//                            queueInfo.setUnConsumeMessageGrowthTimes(0);
//
//                        }
//
//                        queueInfo.setQueueName(queueName);
//                        queueInfo.setUnConsumeMessageNum(messages);
//                        queueInfo.setQueueConsumers(consumers);
//                        queueInfo.setAllPublishMessageNum(publish);
//                        queueInfo.setAllDeliverMessageNum(deliver);
//                        // add info
//                        dbQueueInfos.add(queueInfo);
//                        // alarm
//                        if (enableEmailAlarm) {
//                            emailWorker.alarm(queueInfo);
//                        }
//                        // transfer json and save to redis
//                        redisTemplate.opsForValue().set(queueName, mapper.writeValueAsString(queueInfo));
//                    }
//                    catch (Exception ex) {
//                        LOGGER.error("", ex);
//                    }
//                    finally {
//                        lock.unlock();
//                    }
//                }
//            }
//            // put history to MySql or SystemProperties
//            long start = System.currentTimeMillis();
//            dataOperater.insertHistoryBatch(dbQueueInfos);
//            LOGGER.info(" insertHistoryBatch cost: " + (System.currentTimeMillis() - start) + " Millis");
//        }
//        catch (Exception ex) {
//            LOGGER.error("", ex);
//        }
//        LOGGER.info(" insertOrUpdateQueueInfoFromRabbitmqToRedis cost: " + (System.currentTimeMillis() - begin)
//                + " Millis");
//    }
//
//    /**
//     * getHttpClient
//     *
//     * @return
//     */
//    private CloseableHttpClient getHttpClient() {
//
//        if (httpclient == null) {
//            httpclient = HttpClients.createDefault();
//        }
//        return httpclient;
//    }
//
//    @Value("${SKYTRAIN_RABBITMQ_HTTP}")
//    protected String rabbitmqHttp;
//
//    @Value("${SKYTRAIN_RABBITMQ_HTTP_USERNAME}")
//    protected String rabbitmqHttpUserName;
//
//    @Value("${SKYTRAIN_RABBITMQ_HTTP_PASSWORD}")
//    protected String rabbitmqHttpPassword;
//
//    /**
//     * getHttpGet
//     *
//     * @return
//     */
//    private HttpGet getHttpGet() {
//
//        if (httpget == null) {
//            httpget = new HttpGet(rabbitmqHttp);
//            byte[] credentials = Base64
//                    .encodeBase64((rabbitmqHttpUserName + ":" + rabbitmqHttpPassword).getBytes(StandardCharsets.UTF_8));
//            httpget.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
//        }
//        return httpget;
//    }
//
//    /**
//     * queryRabbitmqStringInfo
//     *
//     * @return
//     */
//    private String queryRabbitmqStringInfo() {
//
//        long begin = System.currentTimeMillis();
//        String info = null;
//        CloseableHttpResponse response = null;
//        try {
//            response = getHttpClient().execute(getHttpGet());
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                InputStream instreams = entity.getContent();
//                info = convertStreamToString(instreams);
//            }
//        }
//        catch (ClientProtocolException ex) {
//            LOGGER.error("", ex);
//        }
//        catch (IOException ex) {
//            LOGGER.error("", ex);
//        }
//        finally {
//            try {
//                if (response != null) {
//                    response.close();
//                }
//            }
//            catch (IOException ex) {
//                LOGGER.error("", ex);
//            }
//        }
//        LOGGER.info(" queryRabbitmqStringInfo cost: " + (System.currentTimeMillis() - begin) + " Millis");
//        return info;
//    }
//
//    /**
//     * convertStreamToString
//     *
//     * @param is
//     * @return
//     */
//    public String convertStreamToString(InputStream is) {
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        StringBuilder sb = new StringBuilder();
//        String line;
//        try {
//            while ((line = reader.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//        }
//        catch (IOException ex) {
//            LOGGER.error("", ex);
//        }
//        finally {
//            try {
//                is.close();
//            }
//            catch (IOException ex) {
//                LOGGER.error("", ex);
//            }
//        }
//        return sb.toString();
//    }
//
//    /**
//     * queryRabbitmqListInfo
//     *
//     * @return
//     */
//    // @Scheduled(initialDelay = 10000L, fixedRate = 60000L)
//    public void UpdateRedisAndInsertMySQL() {
//
//        long begin = System.currentTimeMillis();
//
//        try {
//            String info = queryRabbitmqStringInfo();
//
//            ObjectMapper mapper = new ObjectMapper();
//            // JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, Map.class);
//            // List<Map> queues = mapper.readValue(info, javaType);
//            @SuppressWarnings("rawtypes")
//            List<Map> queues = mapper.readValue(info, new TypeReference<List<Map>>() {
//            });
//            LinkedList<String> queueNames = new LinkedList<String>();
//            LinkedList<QueueInfo> queueInfos = getQueueInfoFromMQ(queues, queueNames);
//            LinkedList<QueueInfo> dbQueueInfos = new LinkedList<QueueInfo>();
//            Map<String, String> redisMap = new HashMap<String, String>();
//
//            long start = System.currentTimeMillis();
//            // lock
//            acquire(queueNames);
//            List<String> redisInfo = redisTemplate.opsForValue().multiGet(queueNames);
//            QueueInfo fromMQ;
//            QueueInfo fromRedis;
//
//            for (int i = 0; i < queueNames.size(); i++) {
//                fromMQ = queueInfos.get(i);
//                String queueInfoObject = redisInfo.get(i);
//                if (queueInfoObject != null) {
//                    fromRedis = mapper.readValue(queueInfoObject, QueueInfo.class);
//
//                    // publish
//                    Long publishMessageNum = fromMQ.getAllPublishMessageNum() - fromRedis.getAllPublishMessageNum();
//                    fromRedis.setPublishMessageNum(publishMessageNum.intValue());
//
//                    // deliver
//                    Long deliverMessageNum = fromMQ.getAllDeliverMessageNum() - fromRedis.getAllDeliverMessageNum();
//                    fromRedis.setDeliverMessageNum(deliverMessageNum.intValue());
//
//                    // incTimes
//                    if (fromMQ.getUnConsumeMessageNum() > fromRedis.getUnConsumeMessageNum()) {
//                        int lastIncTimes = fromRedis.getUnConsumeMessageGrowthTimes();
//                        fromRedis.setUnConsumeMessageGrowthTimes(lastIncTimes + 1);
//
//                    }
//                    else {
//                        fromRedis.setUnConsumeMessageGrowthTimes(0);
//
//                    }
//
//                    fromRedis.setQueueName(fromMQ.getQueueName());
//                    fromRedis.setUnConsumeMessageNum(fromMQ.getUnConsumeMessageNum());
//                    fromRedis.setQueueConsumers(fromMQ.getQueueConsumers());
//                    fromRedis.setAllPublishMessageNum(fromMQ.getAllPublishMessageNum());
//                    fromRedis.setAllDeliverMessageNum(fromMQ.getAllDeliverMessageNum());
//
//                    // alarm
//                    if (enableEmailAlarm) {
//                        emailWorker.alarm(fromRedis);
//                    }
//                    // collect info
//                    redisMap.put(queueNames.get(i), mapper.writeValueAsString(fromRedis));
//                    dbQueueInfos.add(fromRedis);
//                }
//                // 数据第一次存 Redis
//                else {
//                    // publish
//                    Long publishMessageNum = fromMQ.getAllPublishMessageNum();
//                    fromMQ.setPublishMessageNum(publishMessageNum.intValue());
//
//                    // deliver
//                    Long deliverMessageNum = fromMQ.getAllDeliverMessageNum();
//                    fromMQ.setDeliverMessageNum(deliverMessageNum.intValue());
//
//                    // incTimes
//                    fromMQ.setUnConsumeMessageGrowthTimes(0);
//
//                    // alarm
//                    if (enableEmailAlarm) {
//                        emailWorker.alarm(fromMQ);
//                    }
//                    // collect info
//                    redisMap.put(queueNames.get(i), mapper.writeValueAsString(fromMQ));
//                    dbQueueInfos.add(fromMQ);
//                }
//            }
//            // data persistence: updateRedis
//            updateRedis(redisMap);
//            // unlock
//            release(queueNames);
//            LOGGER.info(" Redis total cost: " + (System.currentTimeMillis() - start) + " Millis");
//            // data persistence: insertMySQL
//            insertMySQL(dbQueueInfos);
//
//            LOGGER.info(" UpdateRedisAndInsertMySQL total cost: " + (System.currentTimeMillis() - begin) + " Millis");
//        }
//        catch (Exception ex) {
//            LOGGER.error("", ex);
//        }
//
//    }
//
//    @SuppressWarnings("rawtypes")
//    private LinkedList<QueueInfo> getQueueInfoFromMQ(List<Map> queues, LinkedList<String> queueNames) {
//
//        int tmpQueues = 0;
//        int hasNoConsumers = 0;
//        // 临时变量
//        String queueName;
//        Integer messages;
//        Integer consumers;
//        Integer publishTemp;
//        Integer deliverTemp;
//        Map<?, ?> messageStatsMap;
//        Map<?, ?> temporary;
//
//        // LinkedList 用来保证次序
//        LinkedList<QueueInfo> queueInfos = new LinkedList<QueueInfo>();
//        for (Map<?, ?> queue : queues) {
//            /**
//             * arguments 字段区分是否是临时队列，临时队列带有参数 "x-expires": 30000 durable 字段不能区分
//             */
//            temporary = (Map<?, ?>) queue.get("arguments");
//            if (!temporary.isEmpty()) {
//                tmpQueues++;
//                continue;
//            }
//            queueName = (String) queue.get("name");
//            queueNames.addLast(queueName);
//            messages = (Integer) queue.get("messages");
//            consumers = (Integer) queue.get("consumers");
//            if (consumers <= 0) {
//                hasNoConsumers++;
//            }
//
//            messageStatsMap = (Map<?, ?>) queue.get("message_stats");
//            Long publish = new Long(0);
//            Long deliver = new Long(0);
//            if (messageStatsMap != null) {
//                publishTemp = (Integer) messageStatsMap.get("publish");
//                publish = Long.valueOf(publishTemp == null ? new Long(0) : publishTemp);
//                deliverTemp = (Integer) messageStatsMap.get("deliver_get");
//                deliver = Long.valueOf(deliverTemp == null ? new Long(0) : deliverTemp);
//            }
//
//            QueueInfo queueInfo = new QueueInfo();
//            queueInfo.setQueueName(queueName);
//            queueInfo.setUnConsumeMessageNum(messages);
//            queueInfo.setQueueConsumers(consumers);
//            queueInfo.setAllPublishMessageNum(publish);
//            queueInfo.setAllDeliverMessageNum(deliver);
//            // add info, order is required
//            queueInfos.addLast(queueInfo);
//        }
//        LOGGER.info(" RabbitMQ has " + tmpQueues + " temporary queues");
//        LOGGER.info(" RabbitMQ has " + hasNoConsumers + " queues with no consumer");
//        return queueInfos;
//    }
//
//    private void updateRedis(Map<String, String> redisMap) {
//
//        long start = System.currentTimeMillis();
//        try {
//            redisTemplate.opsForValue().multiSet(redisMap);
//        }
//        catch (Exception ex) {
//            LOGGER.error("", ex);
//        }
//        LOGGER.info(" updateRedis cost: " + (System.currentTimeMillis() - start) + " Millis");
//    }
//
//    private void insertMySQL(List<QueueInfo> dbQueueInfos) {
//
//        long start = System.currentTimeMillis();
//        try {
//            dataOperater.insertHistoryBatch(dbQueueInfos);
//        }
//        catch (Exception ex) {
//            LOGGER.error("", ex);
//        }
//        LOGGER.info(" insertMysql cost: " + (System.currentTimeMillis() - start) + " Millis");
//    }
//
//    private void acquire(List<String> queueNames) {
//
//        long start = System.currentTimeMillis();
//        if (queueNames == null || queueNames.isEmpty()) {
//            return;
//        }
//        for (String queueName : queueNames) {
//            Lock lock = RedisLock.getLock(queueName);
//            lock.lock();
//        }
//        LOGGER.info(" acquire locks cost: " + (System.currentTimeMillis() - start) + " Millis");
//    }
//
//    private void release(List<String> queueNames) {
//
//        long start = System.currentTimeMillis();
//        if (queueNames == null || queueNames.isEmpty()) {
//            return;
//        }
//        for (String queueName : queueNames) {
//            Lock lock = RedisLock.getLock(queueName);
//            lock.unlock();
//        }
//        LOGGER.info(" release locks cost: " + (System.currentTimeMillis() - start) + " Millis");
//    }
//}