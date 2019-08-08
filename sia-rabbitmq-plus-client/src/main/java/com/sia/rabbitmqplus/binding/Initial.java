package com.sia.rabbitmqplus.binding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author xinliang on 16/8/24.
 */
public class Initial {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initial.class);
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean READY = new AtomicBoolean(false);
    private static final CountDownLatch START_GATE = new CountDownLatch(1);
    private static final String PARAMETER_FILE = "siaparameters.properties";
    private static final Pattern PATTERN = Pattern.compile("^(v-)?.*(.list|([0-9])*)@(creditease.cn|yirendai.com)$",
            Pattern.CASE_INSENSITIVE);
    protected static String projectName;
    protected static String projectDescription;
    protected static String emailReceviers;
    protected static String localIpAddress;

    private Initial() {

    }

    static {
        try {
            localIpAddress = NetworkHelper.getServerIp();
        } catch (Exception e) {
            LOGGER.error(Const.SIA_LOG_PREFIX, e);
        }
    }

    /**
     * getprojectName
     *
     * @return
     */
    public static String getProjectName() {

        if (projectName == null || "".endsWith(projectName)) {
            init();
        }
        return projectName;
    }

    /**
     * init
     */
    protected static void init() {

        if (STARTED.compareAndSet(false, true)) {
            Properties prop = PropertyHelper.load(PARAMETER_FILE);
            LOGGER.info(Const.SIA_LOG_PREFIX + "[======配置文件<" + PARAMETER_FILE + ">的内容======]");
            Set<Object> keys = prop.keySet();
            for (Object o : keys) {
                LOGGER.info(Const.SIA_LOG_PREFIX + "[" + o + "=" + prop.get(o) + "]");
            }
            LOGGER.info(Const.SIA_LOG_PREFIX + "[======<" + PARAMETER_FILE + ">内容结束======]");
            String serverIP = prop.getProperty("RABBITMQ_HOST");
            String port = prop.getProperty("RABBITMQ_PORT");
            if (PropertyHelper.isEmpty(serverIP)) {
                LOGGER.error(Const.SIA_LOG_PREFIX + "[服务器地址为空，请检查配置文件]");
                return;
            }
            if (PropertyHelper.isEmpty(port)) {
                LOGGER.error(Const.SIA_LOG_PREFIX + "[服务器地址端口为空，请检查配置文件]");
                return;
            }

            projectName = prop.getProperty("PROJECT_NAME");
            if (PropertyHelper.isEmpty(projectName)) {
                LOGGER.error(Const.SIA_LOG_PREFIX + "[SIA启动失败,没有填写项目名称]");
                return;
            }
            projectName = projectName.trim();
            projectDescription = prop.getProperty("PROJECT_DESCRIPTION");
            emailReceviers = prop.getProperty("EMAIL_RECEVIERS");
            if (PropertyHelper.isEmpty(emailReceviers)) {
                LOGGER.error(Const.SIA_LOG_PREFIX + "[SIA启动失败,没有填写报警email地址]");
                return;
            }
            emailReceviers = emailReceviers.trim();
            if (checkCrediteaseEmail(emailReceviers)) {
                String root = prop.getProperty("SKYTRAIN_LOG_ROOT");
                String fileSize = prop.getProperty("SKYTRAIN_LOG_FILESIZE");
                String fileNums = prop.getProperty("SKYTRAIN_LOG_FILENUMS");
                setLogRoot(root);
                setLogSize(fileSize);
                setLogNum(fileNums);

                Const.RABBIT_HOST = serverIP.trim();
                Const.RABBIT_PORT = Integer.parseInt(port.trim());
                if (ChannelPool.init()) {
                    LOGGER.info(Const.SIA_LOG_PREFIX + "[======配置文件<" + PARAMETER_FILE + ">加载成功======]");
                    ClientDataGather.startClientDataGather();
                    READY.compareAndSet(false, true);
                    release();
                }
            }

        }
    }

    public static void await() {

        try {
            START_GATE.await(60000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(Const.SIA_LOG_PREFIX + "[等待与服务器建立连接时出错]" + e.getMessage());
        }

    }

    private static void release() {

        START_GATE.countDown();

    }

    public static boolean isReady() {

        return READY.get();
    }

    private static void setLogRoot(String root) {

        if (root == null) {
            LOGGER.info(Const.SIA_LOG_PREFIX + "[默认消息日志输出路径]");
        } else {
            Const.SKYTRAIN_LOG_ROOT = root.trim();
            if (!Const.SKYTRAIN_LOG_ROOT.endsWith("/")) {
                Const.SKYTRAIN_LOG_ROOT += "/";
            }
            LOGGER.info(Const.SIA_LOG_PREFIX + "[指定消息日志输出路径:<" + Const.SKYTRAIN_LOG_ROOT + ">]");
        }
    }

    private static void setLogSize(String fileSize) {

        if (fileSize == null) {
            LOGGER.info(Const.SIA_LOG_PREFIX + "[默认消息日志大小：20M]");
        } else {
            Const.SKYTRAIN_LOG_FILESIZE = fileSize.trim();
            LOGGER.info(Const.SIA_LOG_PREFIX + "[指定消息日志大小：" + Const.SKYTRAIN_LOG_FILESIZE + "]");
        }
    }

    private static void setLogNum(String fileNums) {

        if (fileNums == null) {
            LOGGER.info(Const.SIA_LOG_PREFIX + "[默认消息日志个数：10]");
        } else {
            int size = 10;
            try {
                size = Integer.parseInt(fileNums.trim());
            } catch (NumberFormatException e) {
                LOGGER.error(Const.SIA_LOG_PREFIX, e);
            }
            if (size > 0) {
                Const.SKYTRAIN_LOG_FILENUMS = size;
            }
            LOGGER.info(Const.SIA_LOG_PREFIX + "[指定消息日志个数：" + Const.SKYTRAIN_LOG_FILENUMS + "]");
        }
    }

    private static boolean checkCrediteaseEmail(String emails) {

        for (String email : emails.split(",")) {
            if (!PATTERN.matcher(email).matches()) {
                LOGGER.error(Const.SIA_LOG_PREFIX + "[邮箱：<" + email + ">不是宜信公司邮箱，请填写宜信公司邮箱]");
                return false;
            }
        }
        LOGGER.info(Const.SIA_LOG_PREFIX + "[邮箱检查通过]");
        return true;
    }

}
