package com.sia.rabbitmqplus.gather.email;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
/**
 * @author lipengfei
 */
@FeignClient(name = "gantry-email-alarm")
public interface EmailService {

    /**
     * 发送预警邮件
     * 我们是通过feign调邮件接口发送邮件的，您需自己实现邮件发送接口
     * @param json
     * @return
     */
    @RequestMapping(value = "/v1/sendEmail", method = RequestMethod.POST)
    public String sendEmail(@RequestBody String json);

}