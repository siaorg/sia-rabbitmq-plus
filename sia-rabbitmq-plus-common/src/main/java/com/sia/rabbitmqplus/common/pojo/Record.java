package com.sia.rabbitmqplus.common.pojo;

/**
 * @author xinliang on 16/11/16.
 */
public class Record {

    private int id;
    private String queueName;
    private int unConsumeMessageNum;
    private int publishMessageNum;
    private int deliverMessageNum;
    private String worktime;

    private int publishMessageNumSum;
    private int deliverMessageNumSum;

    private String realtime;

    private String startTime;
    private String endTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getUnConsumeMessageNum() {
        return unConsumeMessageNum;
    }

    public void setUnConsumeMessageNum(int unConsumeMessageNum) {
        this.unConsumeMessageNum = unConsumeMessageNum;
    }

    public int getPublishMessageNum() {
        return publishMessageNum;
    }

    public void setPublishMessageNum(int publishMessageNum) {
        this.publishMessageNum = publishMessageNum;
    }

    public int getDeliverMessageNum() {
        return deliverMessageNum;
    }

    public void setDeliverMessageNum(int deliverMessageNum) {
        this.deliverMessageNum = deliverMessageNum;
    }

    public String getWorktime() {
        return worktime;
    }

    public void setWorktime(String worktime) {
        this.worktime = worktime;
    }

    public int getPublishMessageNumSum() {
        return publishMessageNumSum;
    }

    public void setPublishMessageNumSum(int publishMessageNumSum) {
        this.publishMessageNumSum = publishMessageNumSum;
    }

    public int getDeliverMessageNumSum() {
        return deliverMessageNumSum;
    }

    public void setDeliverMessageNumSum(int deliverMessageNumSum) {
        this.deliverMessageNumSum = deliverMessageNumSum;
    }

    public String getRealtime() {
        return realtime;
    }

    public void setRealtime(String realtime) {
        this.realtime = realtime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}


