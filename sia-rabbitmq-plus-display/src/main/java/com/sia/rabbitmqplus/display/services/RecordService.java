package com.sia.rabbitmqplus.display.services;


import com.sia.rabbitmqplus.display.mapper.RecordMapper;
import com.sia.rabbitmqplus.display.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by xinliang on 16/11/16.
 */
@Service
public class RecordService {

    @Autowired
    private RecordMapper recordMapper;

    /**
     * findAsMinute
     *
     * @param record
     * @return
     */
    public List<Record> findAsMinute(Record record) {

        List<Record> list = recordMapper.findAsMinute(record);
        return list;
    }

    /**
     * findAsHour
     *
     * @param record
     * @return
     */
    public List<Record> findAsHour(Record record) {

        List<Record> list = recordMapper.findAsHour(record);
        return list;
    }

    /**
     * findAsDay
     *
     * @param record
     * @return
     */
    public List<Record> findAsDay(Record record) {

        List<Record> list = recordMapper.findAsDay(record);
        return list;
    }

}
