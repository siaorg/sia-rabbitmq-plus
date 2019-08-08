package com.sia.rabbitmqplus.gather.email;

import com.sia.rabbitmqplus.common.helpers.JSONHelper;
import com.sia.rabbitmqplus.common.pojo.QueueInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lipengfei
 */
@Component
public class EmailWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailWorker.class);

    @Value("${SKYTRAIN_DEFAULT_EMAIL}")
    protected String adminEmailers;

    @Autowired
    private EmailService emailService;

    /**
     *
     * @param queueInfo
     * @return
     */
    public boolean alarm(QueueInfo queueInfo) {

        if (queueInfo.getUnConsumeMessageNum() > queueInfo.getUnConsumeMessageAlarmNum()
                || queueInfo.getUnConsumeMessageGrowthTimes() > queueInfo.getUnConsumeMessageAlarmGrowthTimes()) {
            return sendEmail(queueInfo);
        }
        return false;
    }

    private static final long DEFAULT_30MIN = 30 * 60 * 1000L;

    /**
     * 发送报警邮件
     *
     * @param queueInfo
     * @return
     */
    private boolean sendEmail(QueueInfo queueInfo) {

        buildEmailInfo(queueInfo);
        List<String> mailto = new ArrayList<String>();
        String[] previous = queueInfo.getProjectInfo().getEmailReceviers();
        for (int i = 0, n = previous.length; i < n; i++) {
            if (!mailto.contains(previous[i])) {
                mailto.add(previous[i]);
            }
            if (mailto.contains("")) {
                mailto.remove("");
            }
        }
        String[] admin = adminEmailers.split(",");
        for (int i = 0, n = admin.length; i < n; i++) {
            if (!mailto.contains(admin[i])) {
                mailto.add(admin[i]);
            }
            if (mailto.contains("")) {
                mailto.remove("");
            }
        }

        String subject = queueInfo.getEmailSubject();
        String content = queueInfo.getEmailContent();
        String primary = queueInfo.getQueueName();
        String resp = emailAlarm(subject, mailto, content, primary, DEFAULT_30MIN);
        LOGGER.info("send email to :{}, and response is {}", JSONHelper.toString(mailto), resp);
        return true;
    }

    private String exception2String(Throwable e) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    private static final String EXCEPTION = "SOS";
    private static final long NOLIMIT = 1L;

    public void sendSOS(Exception e) {

        String content = exception2String(e);

        List<String> mailto = new ArrayList<String>();
        String[] admin = adminEmailers.split(",");
        for (int i = 0, n = admin.length; i < n; i++) {
            if (!mailto.contains(admin[i])) {
                mailto.add(admin[i]);
            }
            if (mailto.contains("")) {
                mailto.remove("");
            }
        }
        String subject = "程序运行异常";
        String resp = emailAlarm(subject, mailto, content, EXCEPTION, NOLIMIT);
        LOGGER.info("send email to :{}, and response is {}", JSONHelper.toString(mailto), resp);

    }

    private String emailAlarm(String subject, List<String> mailto, String content, String primary, long elapse) {

        Map<String, Object> data = new HashMap<String, Object>(8);
        data.put("subject", subject);
        data.put("mailto", mailto);
        data.put("content", content);
        data.put("primary", primary);
        data.put("elapse", elapse);
        String json = JSONHelper.toString(data);
        return emailService.sendEmail(json);
    }

    /**
     * 构造发送邮件信息
     *
     * @param queueInfo
     */
    private static void buildEmailInfo(QueueInfo queueInfo) {

        queueInfo.setEmailSubject("SIA预警邮件");
        queueInfo.setEmailContent("SIA队列报警：" + "<br>" + "<pre><div><div><h4> SIA报警信息 —— 队列:" + queueInfo.getQueueName()
                + "报警</h4></div><div><tbody><tr><td> 报警指标内容:</td>" + "<td><strong><font color=\"red\">" + " 剩余消息数："
                + queueInfo.getUnConsumeMessageNum() + " 报警阈值：" + queueInfo.getUnConsumeMessageAlarmNum() + " 增长次数:"
                + queueInfo.getUnConsumeMessageGrowthTimes() + " 报警增长次数阈值："
                + queueInfo.getUnConsumeMessageAlarmGrowthTimes()
                + "</font></strong></td></tr></tbody></div><div><h5></h5></div></div>" + "</pre>");
    }
}
