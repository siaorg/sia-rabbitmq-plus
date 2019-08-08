package com.sia.rabbitmqplus.display.webapp.dao;

import com.sia.rabbitmqplus.display.webapp.pojo.Record;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xinliang on 16/11/16.
 */
public class RecordMapperImpl implements RecordMapper {

    @Resource(name = "sqlSessionFactory")
    private SqlSessionFactory sqlSessionFactory;

    /**
     * findAsMinute
     *
     * @param record
     * @return
     */
    public List<Record> findAsMinute(Record record) {
        SqlSession session = sqlSessionFactory.openSession();
        List<Record> list = null;
        try {
            list = session.selectList("findAsMinute", record);
        } finally {
            session.close();
        }
        return list;
    }

    /**
     * findAsHour
     *
     * @param record
     * @return
     */
    public List<Record> findAsHour(Record record) {
        SqlSession session = sqlSessionFactory.openSession();
        List<Record> list = null;
        try {
            list = session.selectList("findAsHour", record);
        } finally {
            session.close();
        }
        return list;
    }

    /**
     * findAsDay
     *
     * @param record
     * @return
     */
    public List<Record> findAsDay(Record record) {
        SqlSession session = sqlSessionFactory.openSession();
        List<Record> list = null;
        try {
            list = session.selectList("findAsDay", record);
        } finally {
            session.close();
        }
        return list;
    }

}
