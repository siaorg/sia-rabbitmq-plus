package com.sia.rabbitmqplus.business;

import com.sia.rabbitmqplus.binding.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by xinliang on 16/8/3.
 */
public class ExecThreadPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecThreadPool.class);

    private ExecThreadPool() {

    }

    /**
     * SIA线程抛弃策略
     */
    public static class SiaCallerRunsPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code CallerRunsPolicy}.
         */
        public SiaCallerRunsPolicy() {
        }

        /**
         * Executes task r in the caller's thread, unless the executor has been
         * shut down, in which case the task is discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                try {
                    Thread.sleep(1000);
                } catch (java.lang.InterruptedException ex) {
                    LOGGER.error(Const.SIA_LOG_PREFIX, ex);

                }
                e.execute(r);
            }
        }
    }

    /**
     * 异步接收线程池Map
     */
    private static Map<String, ExecutorService> threadPoolMap = new ConcurrentHashMap<String, ExecutorService>();

    /**
     * @param key
     * @param r
     */
    public static void execute(String key, Runnable r) {
        if (threadPoolMap.get(key) == null) {
            final int size = Runtime.getRuntime().availableProcessors();
            ExecutorService pool = new ThreadPoolExecutor(size, size * 2, 30, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(size * 4), new ExecThreadPool.SiaCallerRunsPolicy());
            threadPoolMap.put(key, pool);
        }
        threadPoolMap.get(key).execute(r);
    }

    /**
     * @param key
     * @param pool
     */
    public static void buildThreadPool(String key, ExecutorService pool) {
        if (threadPoolMap.get(key) == null) {
            threadPoolMap.put(key, pool);
        }
    }

    private static Map<String, Integer> prefetchCount = new ConcurrentHashMap<String, Integer>();

    public static void putPrefetchCount(String key, int qos) {
        if (prefetchCount.get(key) == null) {
            prefetchCount.put(key, new Integer(qos));
        }
    }

    public static int getPrefetchCount(String key) {
        if (prefetchCount.get(key) == null) {
            return Runtime.getRuntime().availableProcessors() * 2;
        } else {
            return prefetchCount.get(key).intValue();
        }
    }
    /**
     * 存储ACK的模式，ackModel 回复模式：ACK-服务器清除消息；NACK-消息重回队列
     */
    private static ConcurrentHashMap<String, String> queueAckModel = new ConcurrentHashMap<String, String>();
    
    public static String getAckModel(String queueName) {
        return queueAckModel.get(queueName);
    }
    
    public static String putAckModel(String queueName,String ackModel) {
        return queueAckModel.putIfAbsent(queueName, ackModel);
    }
}
