package com.sia.rabbitmqplus.binding;

import java.util.Properties;

/**
 * @author xinliang
 * @date 16/8/3
 */
public class Const {


	protected static final String PARAMETER_FILE = "siaparameters.properties";
	public static String SIA_LOG_ROOT = "";
	public static String SIA_LOG_FILESIZE = "20MB";
	public static int SIA_LOG_FILENUMS = 10;
	public static final String SIA_DAILY_LOG = "SIA_DAILY_LOG";


	protected static final int CHANNELPOOL_QUEUE_SIZE = 10;
	protected static final String CLASS_NAME = "className";
	protected static final String BEAN_NAME = "beanName";
	protected static final String METHOD_NAME = "methodName";
	protected static final String BEAN_METHOD_NAME = "beanMethodName";
	protected static final String AUTO_ACK = "autoAck";
	protected static final String THREAD_POOL_SIZE = "threadPoolSize";
	public static final String SIA_LOG_PREFIX = "sia_log->";
	protected static final String ACK_MODEL = "ackModel";
	protected static final String ACK = "ACK";
	protected static final String NACK = "NACK";
	protected static final String DEFAULT = "DEFAULT";

	private Const() {

	}

}
