package com.sia.rabbitmqplus.gather;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by xinliang on 2017/10/27.
 */
@FeignClient(name = "rabbitmq-http", url = "${SKYTRAIN_RABBITMQ_HTTP}")
public interface TaskService {

    @RequestMapping(method = RequestMethod.GET)
    //"Authorization" http的授权证书
    public String query(@RequestHeader("Authorization") String token);

//    @RequestMapping(value = "/api/queues/nlp", method = RequestMethod.GET)
//    //"Authorization" http的授权证书
//    public String query(@RequestHeader("Authorization") String token);

}
