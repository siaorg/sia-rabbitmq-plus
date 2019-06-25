package com.sia.rabbitmqplus.heartbeat.pojo;

/**
 * Created by xinliang on 16/11/10.
 */
public class ProjectInfo implements java.io.Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    /**
     * 项目英文名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String projectDescription;

    /**
     * email接受者
     */
    private String[] emailReceviers = {};

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

    public String[] getEmailReceviers() {

        return emailReceviers;
    }

    public void setEmailReceviers(String[] emailReceviers) {

        this.emailReceviers = emailReceviers;
    }

}
