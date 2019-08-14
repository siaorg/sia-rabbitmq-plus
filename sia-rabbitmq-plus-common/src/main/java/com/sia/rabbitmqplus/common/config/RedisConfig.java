package com.sia.rabbitmqplus.common.config;

import com.sia.rabbitmqplus.common.constant.RedisType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * @author  pengfeili
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Autowired
    private Environment environment;

    public RedisConnectionFactory clusterConnectionFactory() {

        JedisPoolConfig poolConfig = getJedisPoolConfig();
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();

        String connectUrl = environment.getProperty("redis.connect.url");
        String[] redisUrls = connectUrl.split(",");
        Set<RedisNode> clusters = new HashSet<RedisNode>(4);
        for (String url : redisUrls) {
            String[] item = url.split(":");
            String host = item[0];
            int port = Integer.valueOf(item[1]);
            RedisNode node = new RedisNode(host, port);
            clusters.add(node);
        }
        clusterConfiguration.setClusterNodes(clusters);

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(clusterConfiguration);
        // password
        jedisConnectionFactory.setPassword(environment.getProperty("redis.password"));
        // pool
        jedisConnectionFactory.setPoolConfig(poolConfig);

        return jedisConnectionFactory;
    }

    public RedisConnectionFactory sentinelConnectionFactory() {

        JedisPoolConfig poolConfig = getJedisPoolConfig();
        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration();
        // master
        sentinelConfiguration.setMaster(environment.getProperty("redis.master"));

        String connectUrl = environment.getProperty("redis.connect.url");
        String[] redisUrls = connectUrl.split(",");
        Set<RedisNode> sentinels = new HashSet<RedisNode>(4);
        for (String url : redisUrls) {
            String[] item = url.split(":");
            String host = item[0];
            int port = Integer.valueOf(item[1]);
            RedisNode node = new RedisNode(host, port);
            sentinels.add(node);
        }
        sentinelConfiguration.setSentinels(sentinels);

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(sentinelConfiguration);
        // password
        jedisConnectionFactory.setPassword(environment.getProperty("redis.password"));
        // pool
        jedisConnectionFactory.setPoolConfig(poolConfig);
        // database index
        jedisConnectionFactory.setDatabase(Integer.parseInt(environment.getProperty("redis.db.index")));

        return jedisConnectionFactory;
    }

    public RedisConnectionFactory singleConnectionFactory() {

        JedisPoolConfig poolConfig = getJedisPoolConfig();

        String connectUrl = environment.getProperty("redis.connect.url");
        String[] item = connectUrl.split(":");
        String hostName = item[0];
        int port = Integer.valueOf(item[1]);

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(hostName);
        jedisConnectionFactory.setPort(port);
        // password
        jedisConnectionFactory.setPassword(environment.getProperty("redis.password"));
        // pool
        jedisConnectionFactory.setPoolConfig(poolConfig);
        // database index
        jedisConnectionFactory.setDatabase(Integer.parseInt(environment.getProperty("redis.db.index")));

        return jedisConnectionFactory;
    }

    private JedisPoolConfig getJedisPoolConfig() {

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(Integer.parseInt(environment.getProperty("redis.max.active")));
        poolConfig.setMaxIdle(Integer.parseInt(environment.getProperty("redis.max.idle")));
        poolConfig.setMinIdle(Integer.parseInt(environment.getProperty("redis.min.idle")));
        poolConfig.setMaxWaitMillis(Long.parseLong(environment.getProperty("redis.max.wait")));
        poolConfig.setTestOnBorrow(Boolean.parseBoolean((environment.getProperty("redis.testOnBorrow"))));
        poolConfig.setTestOnReturn(Boolean.parseBoolean((environment.getProperty("redis.testOnReturn"))));
        return poolConfig;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        String type = environment.getProperty("redis.type");
        if (RedisType.SINGLE.equals(type)) {
            return singleConnectionFactory();
        }
        if (RedisType.SENTINEL.equals(type)) {
            return sentinelConnectionFactory();
        }
        if (RedisType.CLUSTER.equals(type)) {
            return clusterConnectionFactory();
        }
        return null;
    }

    @Bean(name = "redisTemplate")
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {

        return new StringRedisTemplate(connectionFactory);
    }
}