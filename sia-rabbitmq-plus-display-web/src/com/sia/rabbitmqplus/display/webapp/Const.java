package com.sia.rabbitmqplus.display.webapp;

import java.util.Map;

/**
 * Created by xinliang on 16/11/16.
 */
public class Const {

    public static Map<String, String> sys = System.getenv();

    /**
     * redis地址
     */
    public static String redisAddress = sys.get("SKYTRAIN_REDIS_ADDRESS") != null ? sys.get("SKYTRAIN_REDIS_ADDRESS") : "redis://@10.100.32.11:6379/0";


}
