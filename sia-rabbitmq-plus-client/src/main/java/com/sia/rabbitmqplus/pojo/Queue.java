package com.sia.rabbitmqplus.pojo;

/**
 * Created by xinliang on 16/11/11.
 */
public class Queue {

	private String queueName;

	/**
	 * type 分为 publish or deliver
	 */
	private String type = "";

	private String publishIp;

	private String publishRecentTime;

	private String deliverIp;

	private String deliverRecentTime;

	private String projectName;

	private String projectDescription;

	private String emailReceviers;

	private int unConsumeMessageAlarmNum = 100;

	private int unConsumeMessageAlarmGrowthTimes = 5;

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPublishIp() {
		return publishIp;
	}

	public void setPublishIp(String publishIp) {
		this.publishIp = publishIp;
	}

	public String getPublishRecentTime() {
		return publishRecentTime;
	}

	public void setPublishRecentTime(String publishRecentTime) {
		this.publishRecentTime = publishRecentTime;
	}

	public String getDeliverIp() {
		return deliverIp;
	}

	public void setDeliverIp(String deliverIp) {
		this.deliverIp = deliverIp;
	}

	public String getDeliverRecentTime() {
		return deliverRecentTime;
	}

	public void setDeliverRecentTime(String deliverRecentTime) {
		this.deliverRecentTime = deliverRecentTime;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public String getEmailReceviers() {
		return emailReceviers;
	}

	public void setEmailReceviers(String emailReceviers) {
		this.emailReceviers = emailReceviers;
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

}
