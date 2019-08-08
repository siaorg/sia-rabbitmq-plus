package com.sia.rabbitmqplus.pojo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author venus
 */
public class SIAMessage implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 历史远程库消息id（自动生成）
     */
    private int messageHistoryId;
    /**
     * 消息id（自动生成）
     */
    private String messageId;
    /**
     * 消息信息
     */
    private String messageInfoClob;
    /**
     * 消息接收时间
     */
    private Timestamp acceptTime;
    /**
     * 发送时间
     */
    private Timestamp sendTime;
    /**
     * 业务code
     */
    private String businessCode;
    /**
     * 服务ip
     */
    private String serverIp;
    /**
     * 以对象发送数据type为"object",以字符串发送数据type为"text/plain"
     */
    private String messageType = "";
    private String queueName;
    /**
     * 错误次数
     */
    private Long errorNum;
    private String receiveQuenenName;
    /**
     * 配置组名称
     */
    private String groupCode;
    /**
     * 发送配置组名称
     */
    private String projectName = "";
    /**
     * 同步调用超时时间默认580s
     */
    private Integer timeout = 580;
    /**
     * pojo参数
     */
    private transient Object pojoParam = null;
    /**
     * 消息发送时间戳 Long
     */
    private Long currentDate;
    /**
     * 临时队列存活时间默认30秒(单位:秒)
     */
    private Integer expires = 30;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getMessageHistoryId() {
        return messageHistoryId;
    }

    public void setMessageHistoryId(int messageHistoryId) {
        this.messageHistoryId = messageHistoryId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageInfoClob() {
        return messageInfoClob;
    }

    public void setMessageInfoClob(String messageInfoClob) {
        this.messageInfoClob = messageInfoClob;
    }

    public Timestamp getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(Timestamp acceptTime) {
        this.acceptTime = acceptTime;
    }

    public Timestamp getSendTime() {
        return sendTime;
    }

    public void setSendTime(Timestamp sendTime) {
        this.sendTime = sendTime;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    /**
     * 以对象发送数据type为"object",以字符串发送数据type为"text/plain",默认为"object"
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * 以对象发送数据type为"object",以字符串发送数据type为"text/plain",默认为"object"
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Long getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(Long errorNum) {
        this.errorNum = errorNum;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * old version, 使用 {@code}getReceiveQueueName()
     *
     * @return
     * @deprecated
     */
    @Deprecated
    public String getReceiveQuenenName() {
        return receiveQuenenName;
    }

    /**
     * old version, 使用 {@code}setReceiveQueueName(String receiveQueueName)
     *
     * @param receiveQuenenName
     * @deprecated
     */
    @Deprecated
    public void setReceiveQuenenName(String receiveQuenenName) {
        this.receiveQuenenName = receiveQuenenName;
    }

    public Object getPojoParam() {
        return pojoParam;
    }

    public void setPojoParam(Object pojoParam) {
        this.pojoParam = pojoParam;
    }

    public Long getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Long currentDate) {
        this.currentDate = currentDate;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getExpires() {
        return expires;
    }

    public void setExpires(Integer expires) {
        this.expires = expires;
    }

    public String getReceiveQueueName() {
        return receiveQuenenName;
    }

    public void setReceiveQueueName(String receiveQueueName) {
        this.receiveQuenenName = receiveQueueName;
    }

    @Override
    public String toString() {
        return "groupCode:" + groupCode + "  businessCode:" + businessCode + "  messageId:" + messageId
                + "  messageInfoClob:" + messageInfoClob;
    }

}
