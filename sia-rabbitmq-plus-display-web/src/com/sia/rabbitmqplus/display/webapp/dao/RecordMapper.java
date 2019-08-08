package com.sia.rabbitmqplus.display.webapp.dao;

import com.sia.rabbitmqplus.display.webapp.pojo.Record;

import java.util.List;

/**
 * @author xinliang on 16/11/16.
 */
public interface RecordMapper {

    public List<Record> findAsMinute(Record record);

    public List<Record> findAsHour(Record record);

    public List<Record> findAsDay(Record record);

}
