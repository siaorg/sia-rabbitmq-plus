package com.sia.rabbitmqplus.gather.database;

import com.sia.rabbitmqplus.gather.pojo.QueueInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author pengfeili23
 */
@Component
public class DataBaseHandler {

    @Autowired
    @Qualifier("skytrainJdbcTemplate")
    protected JdbcTemplate skytrainJdbcTemplate;

    /**
     * MQ队列信息
     */
    private static String INSERT_QUEUE_INFO_SQL = "INSERT INTO skytrain_queue_message_info_history "
            + "(queue_name,un_consume_message_num,publish_message_num,deliver_message_num,worktime) "
            + "VALUES (?,?,?,?,?) ";

    private static String DELETE_QUEUE_INFO_SQL = "DELETE FROM skytrain_queue_message_info_history "
            + "WHERE worktime < ? ";

    /**
     * 死信队列数据
     */
    private static String INSERT_DEAD_LETTER_SQL = "INSERT INTO skytrain_dead_letter "
            + "(queue_name,deadtime,type,message) " + "VALUES (?,?,?,?)";

    /**
     * insertHistoryBatch
     *
     * @param queueInfos
     */
    public void insertHistoryBatch(final List<QueueInfo> queueInfos) {

        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        skytrainJdbcTemplate.batchUpdate(INSERT_QUEUE_INFO_SQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement psts, int i) throws SQLException {

                QueueInfo queueInfo = queueInfos.get(i);
                psts.setString(1, queueInfo.getQueueName());
                psts.setInt(2, queueInfo.getUnConsumeMessageNum());
                psts.setInt(3, queueInfo.getPublishMessageNum());
                psts.setInt(4, queueInfo.getDeliverMessageNum());
                psts.setTimestamp(5, ts);
            }

            @Override
            public int getBatchSize() {

                return queueInfos.size();
            }
        });
    }

    /**
     * insertDeadLetter
     *
     * @param queueName
     * @param time
     * @param type
     * @param message
     */
    public void insertDeadLetter(final String queueName, final Timestamp time, final String type,
            final String message) {

        skytrainJdbcTemplate.update(INSERT_DEAD_LETTER_SQL, new PreparedStatementSetter() {

            public void setValues(PreparedStatement psts) throws SQLException {

                psts.setString(1, queueName);
                psts.setTimestamp(2, time);
                psts.setString(3, type);
                psts.setString(4, message);
            }
        });
    }

    /**
     * clearHistory
     */
    public void clearHistory(long millisecondsBefore) {

        final Timestamp ts = new Timestamp(System.currentTimeMillis() - millisecondsBefore);
        skytrainJdbcTemplate.update(DELETE_QUEUE_INFO_SQL, new PreparedStatementSetter() {

            public void setValues(PreparedStatement psts) throws SQLException {

                psts.setTimestamp(1, ts);
            }
        });
    }

    /**
     * 备库中查询数据条数
     *
     * @param SQL
     * @return
     */
    public long getNumFromOtherDataSource(String SQL) {

        return skytrainJdbcTemplate.queryForObject(SQL, Long.class);
    }

}
