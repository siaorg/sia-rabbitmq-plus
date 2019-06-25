package com.sia.rabbitmqplus.display.webapp.pojo;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by xinliang on 16/11/10.
 */
public class QueueInfo implements java.io.Serializable {

    /**
     * 队列名
     */
    private String queueName;

    /**
     * 主题
     */
    private String emailSubject;

    /**
     * 内容
     */
    private String emailContent;

    /**
     * 附件
     */
    private String[] emailAttachFileNames;

    /**
     * 残留消息数量
     */
    private int unConsumeMessageNum;

    /**
     * 队列消费者数量
     */
    private int queueConsumers;

    /**
     * 队列上次活动时间
     */
    private long lastActiveTime;

    /**
     * 发送消息数量
     */
    private int publishMessageNum;

    /**
     * 历史发送消息数量
     */
    private long allPublishMessageNum;

    /**
     * 消费消息数量
     */
    private int deliverMessageNum;

    /**
     * 历史消费消息数量
     */
    private long allDeliverMessageNum;


    /**
     * 残留消息增长次数
     */
    private int unConsumeMessageGrowthTimes;

    /**
     * 报警残留消息数量
     * 默认值100
     */
    private int unConsumeMessageAlarmNum = 100;

    /**
     * 报警残留消息增长次数
     * 默认值5
     */
    private int unConsumeMessageAlarmGrowthTimes = 5;

    /**
     * 给队列发送的IP地址
     */
    private transient Map<String, String> publishIps = new TreeMap<String, String>();

    /**
     * 从队列接收的IP地址
     */
    private transient Map<String, String> deliverIps = new TreeMap<String, String>();

    /**
     * 队列所属工程
     */
    private transient ProjectInfo projectInfo = new ProjectInfo();


    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public String[] getEmailAttachFileNames() {
        return emailAttachFileNames;
    }

    public void setEmailAttachFileNames(String[] emailAttachFileNames) {
        this.emailAttachFileNames = emailAttachFileNames;
    }

    public int getUnConsumeMessageNum() {
        return unConsumeMessageNum;
    }

    public void setUnConsumeMessageNum(int unConsumeMessageNum) {
        this.unConsumeMessageNum = unConsumeMessageNum;
    }

    public int getQueueConsumers() {
        return queueConsumers;
    }

    public void setQueueConsumers(int queueConsumers) {
        this.queueConsumers = queueConsumers;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
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

    public int getUnConsumeMessageGrowthTimes() {
        return unConsumeMessageGrowthTimes;
    }

    public void setUnConsumeMessageGrowthTimes(int unConsumeMessageGrowthTimes) {
        this.unConsumeMessageGrowthTimes = unConsumeMessageGrowthTimes;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public int getUnConsumeMessageAlarmNum() {
        return unConsumeMessageAlarmNum;
    }

    public void setUnConsumeMessageAlarmNum(int unConsumeMessageAlarmNum) {
        this.unConsumeMessageAlarmNum = unConsumeMessageAlarmNum;
    }

    public int getUnConsumeMessageAlarmGrowthTimes() {
        return unConsumeMessageAlarmGrowthTimes;
    }

    public void setUnConsumeMessageAlarmGrowthTimes(int unConsumeMessageAlarmGrowthTimes) {
        this.unConsumeMessageAlarmGrowthTimes = unConsumeMessageAlarmGrowthTimes;
    }

    public long getAllPublishMessageNum() {
        return allPublishMessageNum;
    }

    public void setAllPublishMessageNum(long allPublishMessageNum) {
        this.allPublishMessageNum = allPublishMessageNum;
    }

    public long getAllDeliverMessageNum() {
        return allDeliverMessageNum;
    }

    public void setAllDeliverMessageNum(long allDeliverMessageNum) {
        this.allDeliverMessageNum = allDeliverMessageNum;
    }

    public Map<String, String> getPublishIps() {
        return publishIps;
    }

    public void setPublishIps(Map<String, String> publishIps) {
        this.publishIps = publishIps;
    }

    public Map<String, String> getDeliverIps() {
        return deliverIps;
    }

    public void setDeliverIps(Map<String, String> deliverIps) {
        this.deliverIps = deliverIps;
    }

}
