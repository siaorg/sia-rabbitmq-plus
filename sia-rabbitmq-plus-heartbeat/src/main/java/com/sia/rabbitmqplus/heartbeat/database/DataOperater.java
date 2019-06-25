//package com.creditease.skytrain.supervise.database;
//
//import QueueInfo;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.jdbc.core.BatchPreparedStatementSetter;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Repository;
//
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.List;
//
///**
// * Created by xinliang on 16/11/14.
// */
//@Repository
//public class DataOperater {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(DataOperater.class);
//
//    @Autowired
//    @Qualifier("skytrainJdbcTemplate")
//    protected JdbcTemplate skytrainJdbcTemplate;
//
//    /**
//     * insert sql
//     */
//    private static String insertSql = "INSERT INTO skytrain_queue_message_info_history "
//            + "(queue_name,un_consume_message_num,publish_message_num,deliver_message_num,worktime) "
//            + "VALUES (?,?,?,?,?)";
//
//    /**
//     * insertHistory
//     *
//     * @param queueName
//     * @param unConsumeMessageNum
//     * @param publishMessageNum
//     * @param deliverMessageNum
//     */
//    public void insertHistory(String queueName, int unConsumeMessageNum, int publishMessageNum, int deliverMessageNum,
//            String nowTime) {
//
//        StringBuilder sql = new StringBuilder();
//        sql.append("insert into skytrain_queue_message_info_history");
//        sql.append(" (queue_name,un_consume_message_num,publish_message_num,deliver_message_num,worktime) ");
//        sql.append(" values ");
//        sql.append("('" + queueName + "'," + unConsumeMessageNum + "," + publishMessageNum + "," + deliverMessageNum
//                + ",'");
//        sql.append(Timestamp.valueOf(nowTime) + "')");
//        skytrainJdbcTemplate.update(sql.toString());
//    }
//
//    /**
//     * insertHistoryBatch
//     *
//     * @param queueInfos
//     */
//    public void insertHistoryBatch(final List<QueueInfo> queueInfos) {
//
//        final Timestamp ts = new Timestamp(System.currentTimeMillis());
//        skytrainJdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
//
//            public void setValues(PreparedStatement psts, int i) throws SQLException {
//
//                psts.setString(1, queueInfos.get(i).getQueueName());
//                psts.setInt(2, queueInfos.get(i).getUnConsumeMessageNum());
//                psts.setInt(3, queueInfos.get(i).getPublishMessageNum());
//                psts.setInt(4, queueInfos.get(i).getDeliverMessageNum());
//                psts.setTimestamp(5, ts);
//            }
//
//            public int getBatchSize() {
//
//                return queueInfos.size();
//            }
//        });
//    }
//
//    /**
//     * insertDeadLetter
//     *
//     * @param queueName
//     * @param time
//     * @param type
//     * @param message
//     */
//    public void insertDeadLetter(String queueName, Timestamp time, String type, String message) {
//
//        StringBuilder sql = new StringBuilder();
//        sql.append("insert into skytrain_dead_letter");
//        sql.append(" (queue_name,deadtime,type,message) ");
//        sql.append(" values ");
//        sql.append("('" + queueName + "','" + time + "','" + type + "','" + message + "')");
//        skytrainJdbcTemplate.update(sql.toString());
//    }
//
//    /**
//     * clearHistory
//     */
//    public void clearHistory() {
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        java.util.Date dateNow = new java.util.Date();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(dateNow);
//        cal.add(Calendar.DAY_OF_MONTH, -6);
//        java.util.Date dateBefore = cal.getTime();
//        String worktime = sdf.format(dateBefore);
//
//        StringBuilder sql = new StringBuilder();
//        sql.append(" delete from skytrain_queue_message_info_history ");
//        sql.append(" where ");
//        sql.append(" worktime < ");
//        sql.append("'" + worktime + " 00:00:00'");
//        LOGGER.info(sql.toString());
//        skytrainJdbcTemplate.update(sql.toString());
//    }
//
//}
