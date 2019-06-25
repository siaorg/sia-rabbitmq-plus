package com.sia.rabbitmqplus.display.controller;

import com.sia.rabbitmqplus.display.pojo.QueueInfo;
import com.sia.rabbitmqplus.display.pojo.Record;
import com.sia.rabbitmqplus.display.services.RecordService;
import com.sia.rabbitmqplus.display.services.ViewService;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

/**
 * Created by xinliang on 16/11/16.
 */
@Controller
public class ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewController.class);

    @Autowired
    private ViewService viewService;

    @RequestMapping(value = "/queues", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queues() {

        String response = null;
        List<QueueInfo> queues = viewService.queryView();
        ObjectMapper mapper = new ObjectMapper();
        try {
            response = mapper.writeValueAsString(queues);
        }
        catch (IOException ex) {
            LOGGER.error("", ex);
        }
        return response;
    }

    @RequestMapping(value = "/queue", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queue(@RequestParam() String queueName) {

        String response = null;
        QueueInfo queue = viewService.queryQueue(queueName);
        ObjectMapper mapper = new ObjectMapper();
        try {
            response = mapper.writeValueAsString(queue);
        }
        catch (IOException ex) {
            LOGGER.error("", ex);
        }
        return response;
    }

    @Autowired
    private RecordService recordService;

    @RequestMapping(value = "/stats", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String stat(@RequestParam() String queueName, String startTime, String endTime) throws IOException {

        Record record = new Record();
        record.setQueueName(queueName);

        List<Record> records = null;
        if (startTime.trim().length() == 10 && endTime.trim().length() == 10) {
            record.setStartTime(startTime + " 00:00:00");
            record.setEndTime(endTime + " 23:59:59");
            records = recordService.findAsDay(record);
        }
        else if (startTime.trim().length() == 13 && endTime.trim().length() == 13) {
            record.setStartTime(startTime + ":00:00");
            record.setEndTime(endTime + ":59:59");
            records = recordService.findAsHour(record);
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(records);
    }

    @RequestMapping(value = "/minutes", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String record(@RequestParam() String queueName, String startTime, String endTime) throws IOException {

        Record record = new Record();
        record.setQueueName(queueName);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        List<Record> records = recordService.findAsMinute(record);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(records);
    }

}
