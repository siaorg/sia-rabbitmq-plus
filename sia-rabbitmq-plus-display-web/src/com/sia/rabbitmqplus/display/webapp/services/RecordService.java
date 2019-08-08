package com.sia.rabbitmqplus.display.webapp.services;

import com.sia.rabbitmqplus.display.webapp.dao.RecordMapperImpl;
import com.sia.rabbitmqplus.display.webapp.pojo.Record;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xinliang on 16/11/16.
 */
public class RecordService {

    @Resource(name = "recordMapperImpl")
    private RecordMapperImpl recordMapperImpl;

    /**
     * findAsMinute
     *
     * @param record
     * @return
     */
    public List<Record> findAsMinute(Record record) {
        List<Record> list = recordMapperImpl.findAsMinute(record);
        return list;
    }

    /**
     * findAsHour
     *
     * @param record
     * @return
     */
    public List<Record> findAsHour(Record record) {
        List<Record> list = recordMapperImpl.findAsHour(record);
        return list;
    }

    /**
     * findAsDay
     *
     * @param record
     * @return
     */
    public List<Record> findAsDay(Record record) {
        List<Record> list = recordMapperImpl.findAsDay(record);
        return list;
    }

}
