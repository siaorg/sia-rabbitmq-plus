package com.sia.rabbitmqplus.gather;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author xinliang on 2017/10/27.
 */
@FeignClient(name = "rabbitmq-http", url = "${SIA_RABBITMQ_HTTP}")
public interface TaskService {

    /**用于调用rabbitmq接口
     * @param token 证书
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    String query(@RequestHeader("Authorization") String token);
}
