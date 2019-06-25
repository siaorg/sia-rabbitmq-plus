package com.sia.rabbitmqplus.display.mapper;

import com.sia.rabbitmqplus.display.pojo.Record;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xinliang on 16/11/16.
 */
@Repository
public class RecordMapper {

    // @Resource(name = "sqlSessionFactory")
    @Autowired
    @Qualifier("sqlSessionFactory")
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
        }
        finally {
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
        }
        finally {
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
        }
        finally {
            session.close();
        }
        return list;
    }

}
