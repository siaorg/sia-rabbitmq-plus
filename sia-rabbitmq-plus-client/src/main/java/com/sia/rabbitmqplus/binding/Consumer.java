package com.sia.rabbitmqplus.binding;

import com.sia.rabbitmqplus.business.ExecThreadPool;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author xinliang on 16/8/10.
 */
public class Consumer {

	private Consumer() {

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
	private static final AtomicBoolean STARTED = new AtomicBoolean(false);
	private static ApplicationContext appContext = null;
	private static final String RECEIVE_QUEUE_FILE = "receivequeue.properties";
	private static final int MAX_QUEUE_NUM = getMaxQueueNum();
	private static int queueNum = 1;

	/**
	 * init
	 */
	private static void init() {
		Properties pro = PropertyHelper.load(RECEIVE_QUEUE_FILE);
		LOGGER.info(Const.SIA_LOG_PREFIX + "[======配置文件<" + RECEIVE_QUEUE_FILE + ">的内容======]");
		Set<Object> keys = pro.keySet();
		for (Object o : keys) {
			LOGGER.info(Const.SIA_LOG_PREFIX + "[" + o + "=" + pro.get(o) + "]");
		}
		LOGGER.info(Const.SIA_LOG_PREFIX + "[======<" + RECEIVE_QUEUE_FILE + ">内容结束======]");
		Enumeration<?> en = pro.propertyNames();
		List<String> pass = new ArrayList<String>();
		List<String> fail = new ArrayList<String>();
		while (en.hasMoreElements()) {
			String exchangeAndQueueName = (String) en.nextElement();
			String jsonStr = pro.getProperty(exchangeAndQueueName);
			JSONObject jsonObject = JSONObject.fromObject(jsonStr);
			String queueName = getQueueName(exchangeAndQueueName);

			if (checkQueueName(queueName)) {
				LOGGER.info(Const.SIA_LOG_PREFIX + "[队列名：<" + queueName + "> 检查通过]");
				if (!checkParameter(jsonObject)) {
					LOGGER.error(Const.SIA_LOG_PREFIX + "[队列名为：<" + queueName + "> 的相关配置出错，不予启动！该队列不影响其他队列的启动]");
					fail.add(queueName);
					continue;
				}
				if (checkQueueNum()) {
					LOGGER.info(
							Const.SIA_LOG_PREFIX + "[开始读取队列名为：<" + queueName + "> 的配置，该配置的启动序号为：" + (queueNum++) + "]");
					String exchangeName = getExchangeName(exchangeAndQueueName);

					boolean autoAck = false;
					if (jsonObject.containsKey(Const.AUTO_ACK)) {
						autoAck = jsonObject.getBoolean(Const.AUTO_ACK);
					}
					if(autoAck==false && jsonObject.containsKey(Const.ACK_MODEL)){
					    String ackModel=jsonObject.getString(Const.ACK_MODEL);
					    ExecThreadPool.putAckModel(queueName, ackModel);
					}
					int threadPoolSize = 1;
					if (jsonObject.containsKey(Const.THREAD_POOL_SIZE)) {
						threadPoolSize = jsonObject.getInt(Const.THREAD_POOL_SIZE);
						if (threadPoolSize < 1) {
                            threadPoolSize = 1;
                        }
					}
					buildThreadPool(queueName, threadPoolSize);

					Runnable consumer = null;
					ExecutorService service = Executors.newFixedThreadPool(1);
					if (jsonObject.containsKey(Const.CLASS_NAME)) {
						String className = jsonObject.getString(Const.CLASS_NAME);
						String methodName = jsonObject.getString(Const.METHOD_NAME);
						LOGGER.info(Const.SIA_LOG_PREFIX + "[初始化成功->类名(或beanName):" + className + ",方法名:" + methodName
								+ ",是否自动Ack:" + autoAck + ",线程池大小：" + threadPoolSize + "]");
						consumer = new NormalWorker(exchangeName, queueName, className, methodName, autoAck);
					} else if (jsonObject.containsKey(Const.BEAN_NAME)) {
						String beanName = jsonObject.getString(Const.BEAN_NAME);
						String beanMethodName = jsonObject.getString(Const.BEAN_METHOD_NAME);
						ApplicationContext appContext = Consumer.appContext;
						if (appContext == null) {
							LOGGER.error(Const.SIA_LOG_PREFIX + "[ApplicationContext为空,请指定  ApplicationContext ]");
						} else {
							LOGGER.info(Const.SIA_LOG_PREFIX + "[初始化成功->类名(或beanName):" + beanName + ",方法名:"
									+ beanMethodName + ",是否自动Ack:" + autoAck + ",线程池大小：" + threadPoolSize + "]");
							consumer = new SpringWorker(exchangeName, queueName, beanName, beanMethodName, appContext,
									autoAck);
						}
					} else {
						LOGGER.error(Const.SIA_LOG_PREFIX + "[不可能打印这条信息啊，请务必联系项目组<sia.list@creditease.cn>]");
					}
					service.execute(consumer);
					pass.add(queueName);
				} else {
					LOGGER.error(Const.SIA_LOG_PREFIX + "[<" + queueName + " 超过最大启动队列数限制:<" + MAX_QUEUE_NUM
							+ ">，不予启动！如有必要，请联系项目组<sia.list@creditease.cn>]");
					fail.add(queueName);
				}
			} else {
				LOGGER.error(Const.SIA_LOG_PREFIX + "[<" + queueName + ">名称不符合项目规则,应该以项目名称为前缀]");
				fail.add(queueName);
			}
		}
		LOGGER.info(Const.SIA_LOG_PREFIX + "[======队列启动情况======]");
		LOGGER.info(Const.SIA_LOG_PREFIX + "[启动成功的队列个数]->" + pass.size());
		for (int i = 0; i < pass.size(); i++) {
			LOGGER.info(Const.SIA_LOG_PREFIX + "[启动成功]->" + pass.get(i));
		}
		LOGGER.info(Const.SIA_LOG_PREFIX + "[启动失败的队列个数]->" + fail.size());
		for (int i = 0; i < fail.size(); i++) {
			LOGGER.info(Const.SIA_LOG_PREFIX + "[启动失败]->" + fail.get(i));
		}
		LOGGER.info(Const.SIA_LOG_PREFIX + "[======配置文件<" + RECEIVE_QUEUE_FILE + ">加载成功======]");
	}

	private static boolean checkParameter(JSONObject jsonObject) {
		if (jsonObject.containsKey(Const.CLASS_NAME)) {
			if (!jsonObject.containsKey(Const.METHOD_NAME)) {
				LOGGER.error(Const.SIA_LOG_PREFIX + "[读取配置文件失败，没有关键字]->" + Const.METHOD_NAME);
				return false;
			}
		} else if (jsonObject.containsKey(Const.BEAN_NAME)) {
			if (!jsonObject.containsKey(Const.BEAN_METHOD_NAME)) {
				LOGGER.error(Const.SIA_LOG_PREFIX + "[读取配置文件失败，没有关键字]->" + Const.BEAN_METHOD_NAME);
				return false;
			}
		} else {
			LOGGER.error(Const.SIA_LOG_PREFIX + "[读取配置文件失败，没有关键字]->" + Const.CLASS_NAME + "或" + Const.BEAN_NAME);
			return false;
		}
		return true;
	}

	protected static boolean checkQueueName(String queueName) {
		String queue = queueName.toLowerCase();
		String project = Initial.getProjectName().toLowerCase();
		String[] items = project.split("%");
		for (String item : items) {
			if (queue.startsWith(item)) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkQueueNum() {
		if (queueNum > MAX_QUEUE_NUM) {
			LOGGER.warn(Const.SIA_LOG_PREFIX + "[启动队列数过多，建议拆分系统]");
			return false;
		}
		return true;
	}

	protected static String getQueueName(String exchangeAndQueueName) {
		String queueName;
		if (exchangeAndQueueName.contains("@")) {
			queueName = exchangeAndQueueName.split("@")[1];
		} else {
			queueName = exchangeAndQueueName;
		}
		return queueName;
	}

	private static String getExchangeName(String exchangeAndQueueName) {
		String exchangeName = null;
		if (exchangeAndQueueName.contains("@")) {
			exchangeName = exchangeAndQueueName.split("@")[0];
		}
		return exchangeName;
	}

	private static int getMaxQueueNum() {
		int max = 100;
		String maxQueueNum = System.getProperty("MAX_QUEUE_NUM");
		if (maxQueueNum != null && maxQueueNum.length() > 0) {
			max = Integer.parseInt(maxQueueNum);
		}
		return max;
	}

	private static void buildThreadPool(String queueName, int threadPoolSize) {
	    LinkedBlockingQueue<Runnable> instance=new LinkedBlockingQueue<Runnable>(threadPoolSize * 4);
		ExecutorService pool = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 30, TimeUnit.SECONDS,
				instance, new ExecThreadPool.SiaCallerRunsPolicy());
		ExecThreadPool.buildThreadPool(queueName, pool);
		ExecThreadPool.putPrefetchCount(queueName, threadPoolSize);
		//ConsumerBarrier.initContent(queueName,instance);
	}

	/**
	 * now start
	 */
	public static void start(ApplicationContext applicationContext) {
		Initial.init();
		Initial.await();
		if (Initial.isReady() && STARTED.compareAndSet(false, true)) {
			Consumer.appContext = applicationContext;
			init();
		}

		if (false == STARTED.get()) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "[未成功连接队列服务器，接收端未启动]");
			throw new IllegalStateException("[未成功连接队列服务器，接收端未启动]");
		}

	}

	/**
	 * now start
	 */
	public static void start() {
		Initial.init();
		Initial.await();
		if (Initial.isReady() && STARTED.compareAndSet(false, true)) {
			init();
		}

		if (false == STARTED.get()) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "[未成功连接队列服务器，接收端未启动]");
			throw new IllegalStateException("[未成功连接队列服务器，接收端未启动]");
		}

	}

}
