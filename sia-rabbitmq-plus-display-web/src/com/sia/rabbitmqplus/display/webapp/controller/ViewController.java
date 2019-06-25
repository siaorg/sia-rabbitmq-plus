package com.sia.rabbitmqplus.display.webapp.controller;


import com.alibaba.fastjson.JSON;
import com.sia.rabbitmqplus.display.webapp.pojo.QueueInfo;
import com.sia.rabbitmqplus.display.webapp.pojo.Record;
import com.sia.rabbitmqplus.display.webapp.services.RecordService;
import com.sia.rabbitmqplus.display.webapp.services.ViewService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by xinliang on 16/11/16.
 */
@Controller
public class ViewController {

    @Resource(name = "viewService")
    private ViewService viewService;

    @RequestMapping(value = "/queues", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queues() {
        List<QueueInfo> queues = viewService.queryView();
        return JSON.toJSONString(queues);
    }

    @Resource(name = "recordService")
    private RecordService recordService;

    @RequestMapping(value = "/stats", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String stat(@RequestParam() String queueName, String startTime, String endTime) {
        Record record = new Record();
        record.setQueueName(queueName);

        List<Record> records = null;
        if (startTime.trim().length() == 10 && endTime.trim().length() == 10) {
            record.setStartTime(startTime + " 00:00:00");
            record.setEndTime(endTime + " 23:59:59");
            records = recordService.findAsDay(record);
        } else if (startTime.trim().length() == 13 && endTime.trim().length() == 13) {
            record.setStartTime(startTime + ":00:00");
            record.setEndTime(endTime + ":59:59");
            records = recordService.findAsHour(record);
        }
        return JSON.toJSONString(records);
    }

    @RequestMapping(value = "/minutes", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String record(@RequestParam() String queueName, String startTime, String endTime) {
        Record record = new Record();
        record.setQueueName(queueName);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        List<Record> records = recordService.findAsMinute(record);
        return JSON.toJSONString(records);
    }

}
