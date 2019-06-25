package com.sia.rabbitmqplus.display.services;

import com.sia.rabbitmqplus.display.pojo.QueueInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by xinliang on 16/11/16.
 */
@Service
public class ViewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewService.class);

    @Autowired
    public StringRedisTemplate redisTemplate;

    /**
     * queryView
     *
     * @return
     */
    public List<QueueInfo> queryView() {

        List<QueueInfo> queues = new ArrayList<QueueInfo>();
        Set<String> keys = redisTemplate.keys("*");

        QueueInfo queueInfo;
        for (String key : keys) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                queueInfo = mapper.readValue(redisTemplate.opsForValue().get(key), QueueInfo.class);
                queues.add(queueInfo);
            }
            catch (Exception ex) {
                LOGGER.error("", ex);
            }
        }
        return queues;
    }

    public QueueInfo queryQueue(String queueName) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(redisTemplate.opsForValue().get(queueName), QueueInfo.class);
        }
        catch (Exception ex) {
            LOGGER.error("", ex);
        }
        return null;
    }

}
