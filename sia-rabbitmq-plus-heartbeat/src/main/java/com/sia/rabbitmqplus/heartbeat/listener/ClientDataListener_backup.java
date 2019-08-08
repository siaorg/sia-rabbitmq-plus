package com.sia.rabbitmqplus.heartbeat.listener;
//
//import com.creditease.skytrain.supervise.middleware.RedisLock;
//import QueueInfo;
//import com.rabbitmq.client.*;
//import org.codehaus.jackson.JsonNode;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.locks.Lock;
//
///**
// * @author xinliang on 16/11/11.
// */
//@SpringBootApplication
//public class ClientDataListener_backup implements CommandLineRunner {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDataListener_backup.class);
//
//    @Autowired
//    @Qualifier("redisTemplate")
//    public StringRedisTemplate redisTemplate;
//
//    @Value("${SKYTRAIN_RABBITMQ_HOST}")
//    protected String rabbitmqHost;
//
//    @Value("${SKYTRAIN_RABBITMQ_PORT}")
//    protected int rabbitmqPort;
//
//    @Value("${SKYTRAIN_RABBITMQ_USERNAME}")
//    protected String rabbitmqUsername;
//
//    @Value("${SKYTRAIN_RABBITMQ_PASSWORD}")
//    protected String rabbitmqPassword;
//
//    /**
//     * clientInfoListenerHandler
//     */
//    public void clientInfoListenerHandler() {
//
//        try {
//            String clientListenQueue = "SKYTRAIN_CLIENT_LISTEN_QUEUE";
//            ConnectionFactory factory = new ConnectionFactory();
//            factory.setHost(rabbitmqHost);
//            factory.setPort(rabbitmqPort);
//            factory.setUsername(rabbitmqUsername);
//            factory.setPassword(rabbitmqPassword);
//            factory.setAutomaticRecoveryEnabled(true);
//            factory.setHandshakeTimeout(60 * 1000);
//            LOGGER.info("start to listen MQ->" + rabbitmqHost + "," + rabbitmqPort + "," + rabbitmqUsername + ","
//                    + rabbitmqPassword);
//            Connection connection = factory.newConnection();
//            Channel channel = connection.createChannel();
//
//            channel.queueDeclare(clientListenQueue, true, false, false, null);
//            Consumer consumer = new DefaultConsumer(channel) {
//
//                @Override
//                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
//                        byte[] body) throws IOException {
//
//                    String message = new String(body, "UTF-8");
//                    clientInfoHandler(message);
//                }
//
//                @Override
//                public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
//
//                    LOGGER.error("[网络断开，skytrain尝试重连]", sig);
//                }
//            };
//            channel.basicConsume(clientListenQueue, true, consumer);
//        }
//        catch (Exception ex) {
//            LOGGER.error("", ex);
//        }
//    }
//
//    /**
//     * 监听客户端发送的信息进行处理
//     *
//     * @param info
//     */
//    private void clientInfoHandler(String info) {
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode jsonObject = mapper.readTree(info);
//            String queueName = jsonObject.get("queueName").asText();
//            String type = jsonObject.get("type").asText();
//
//            if ("deliver".equals(type)) {
//                updateDeliver(jsonObject, queueName);
//            }
//            else if ("publish".equals(type)) {
//                updatePublish(jsonObject, queueName);
//            }
//            else {
//                LOGGER.error("[THIS IS A BUG]");
//            }
//        }
//        catch (Exception ex) {
//            LOGGER.error("", ex);
//        }
//    }
//
//    private static void updateEmails(QueueInfo nowQueueInfo, String[] emailReceviers, boolean append) {
//
//        List<String> tempEmails = new ArrayList<String>(
//                Arrays.asList(nowQueueInfo.getProjectInfo().getEmailReceviers()));
//        if (append && tempEmails != null && tempEmails.size() > 0) {
//            for (String email : emailReceviers) {
//                if (!tempEmails.contains(email)) {
//                    tempEmails.add(email);
//                }
//            }
//            nowQueueInfo.getProjectInfo().setEmailReceviers(tempEmails.toArray(new String[tempEmails.size()]));
//            return;
//        }
//        nowQueueInfo.getProjectInfo().setEmailReceviers(emailReceviers);
//
//    }
//
//    private static boolean isEmpty(String str) {
//
//        return str == null || str.length() == 0;
//    }
//
//    private void updatePublish(JsonNode jsonObject, String queueName) {
//
//        String publishIp = jsonObject.get("publishIp").asText();
//        String publishRecentTime = jsonObject.get("publishRecentTime").asText();
//        String[] emailReceviers = (jsonObject.get("emailReceviers").asText()).split(",");
//        Lock lock = RedisLock.getLock(queueName);
//        lock.lock();
//        try {
//            // Object queueInfoObject = redisConfig.getRedisConnection().get(queueName);
//            Object queueInfoObject = redisTemplate.opsForValue().get(queueName);
//
//            ObjectMapper mapper = new ObjectMapper();
//            QueueInfo nowQueueInfo = (queueInfoObject == null) ? new QueueInfo()
//                    : mapper.readValue((String) queueInfoObject, QueueInfo.class);
//
//            Map<String, String> publishIps = nowQueueInfo.getPublishIps();
//            if (publishIps == null) {
//                publishIps = new TreeMap<String, String>();
//            }
//            if (!publishIps.containsKey(publishIp)) {
//                publishIps.put(publishIp, publishRecentTime);
//            }
//            else if (!isEmpty(publishRecentTime)) {
//                publishIps.put(publishIp, publishRecentTime);
//            }
//            nowQueueInfo.setPublishIps(publishIps);
//
//            updateEmails(nowQueueInfo, emailReceviers, false);
//
//            redisTemplate.opsForValue().set(queueName, mapper.writeValueAsString(nowQueueInfo));
//        }
//        catch (Exception ex) {
//            LOGGER.error("", ex);
//        }
//        finally {
//            lock.unlock();
//        }
//    }
//
//    private void updateDeliver(JsonNode jsonObject, String queueName) {
//
//        String projectName = jsonObject.get("projectName").asText();
//        String projectDescription = jsonObject.get("projectDescription").asText();
//        String[] emailReceviers = (jsonObject.get("emailReceviers").asText()).split(",");
//        String deliverIp = jsonObject.get("deliverIp").asText();
//        String deliverRecentTime = jsonObject.get("deliverRecentTime").asText();
//        int unConsumeMessageAlarmNum = jsonObject.get("unConsumeMessageAlarmNum").asInt();
//        int unConsumeMessageAlarmGrowthTimes = jsonObject.get("unConsumeMessageAlarmGrowthTimes").asInt();
//
//        Lock lock = RedisLock.getLock(queueName);
//        lock.lock();
//        try {
//            Object queueInfoObject = redisTemplate.opsForValue().get(queueName);
//
//            ObjectMapper mapper = new ObjectMapper();
//
//            QueueInfo nowQueueInfo = (queueInfoObject == null) ? new QueueInfo()
//                    : mapper.readValue((String) queueInfoObject, QueueInfo.class);
//
//            Map<String, String> deliverIps = nowQueueInfo.getDeliverIps();
//            if (deliverIps == null) {
//                deliverIps = new TreeMap<String, String>();
//            }
//            if (!deliverIps.containsKey(deliverIp)) {
//                deliverIps.put(deliverIp, deliverRecentTime);
//            }
//            else if (!isEmpty(deliverRecentTime)) {
//                deliverIps.put(deliverIp, deliverRecentTime);
//            }
//            nowQueueInfo.setDeliverIps(deliverIps);
//            nowQueueInfo.getProjectInfo().setProjectName(projectName);
//            nowQueueInfo.getProjectInfo().setProjectDescription(projectDescription);
//
//            updateEmails(nowQueueInfo, emailReceviers, false);
//
//            nowQueueInfo.setUnConsumeMessageAlarmNum(unConsumeMessageAlarmNum);
//            nowQueueInfo.setUnConsumeMessageAlarmGrowthTimes(unConsumeMessageAlarmGrowthTimes);
//            redisTemplate.opsForValue().set(queueName, mapper.writeValueAsString(nowQueueInfo));
//        }
//        catch (Exception ex) {
//            LOGGER.error("", ex);
//        }
//        finally {
//            lock.unlock();
//        }
//    }
//
//    public void run(String... strings) throws Exception {
//
//        clientInfoListenerHandler();
//    }
//
//}
