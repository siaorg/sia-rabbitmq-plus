package com.sia.rabbitmqplus.display.webapp.services;

import com.alibaba.fastjson.JSON;
import com.sia.rabbitmqplus.display.webapp.Const;
import com.sia.rabbitmqplus.display.webapp.pojo.QueueInfo;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xinliang on 16/11/16.
 */
public class ViewService {

    private static final Logger LOGGER = Logger.getLogger(ViewService.class);

    private static RedisClient redisClient = null;
    private static RedisConnection<String, String> connection = null;

    /**
     * getRedisConnection
     *
     * @return
     */
    private static synchronized RedisConnection<String, String> getRedisConnection() {
        if (connection == null) {
            redisClient = RedisClient.create(Const.redisAddress);
            connection = redisClient.connect();
        }
        return connection;
    }

    /**
     * close
     */
    private static synchronized void close() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    /**
     * queryView
     *
     * @return
     */
    public List<QueueInfo> queryView() {
        List<QueueInfo> queues = new ArrayList<QueueInfo>();
        List<String> keys = getRedisConnection().keys("*");
        QueueInfo queueInfo;
        for (String key : keys) {
            try {
                queueInfo = JSON.parseObject(getRedisConnection().get(key), QueueInfo.class);
                queues.add(queueInfo);
            } catch (Exception ex) {
                LOGGER.error("", ex);
            }
        }
        return queues;
    }

}
