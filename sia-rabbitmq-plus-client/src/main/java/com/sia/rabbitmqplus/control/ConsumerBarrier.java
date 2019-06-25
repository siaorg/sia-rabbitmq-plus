//
//package com.creditease.skytrain.control;
//
//import java.util.Map.Entry;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.atomic.AtomicBoolean;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class ConsumerBarrier {
//
//    private ConsumerBarrier() {
//
//    }
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBarrier.class);
//    private static final ConcurrentHashMap<String, LinkedBlockingQueue<Runnable>> CONTENT = new ConcurrentHashMap<String, LinkedBlockingQueue<Runnable>>();
//    private static final AtomicBoolean FENCE = new AtomicBoolean(true);
//    private static final Executor EXEC = Executors.newFixedThreadPool(1);
//    private static final Object GUARD = new Object();
//
//    private static boolean isMessageClear() {
//
//        boolean clear = true;
//        for (Entry<String, LinkedBlockingQueue<Runnable>> entry : CONTENT.entrySet()) {
//            String queueName = entry.getKey();
//            LinkedBlockingQueue<Runnable> message = entry.getValue();
//            int size = message.size();
//            if (size > 0) {
//                clear = false;
//                LOGGER.info("QUEUE:[" + queueName + "] STILL HAS [" + size + "] MESSAGES");
//            }
//        }
//        return clear;
//    }
//
//    private static class SelfInspection implements Runnable {
//
//        @Override
//        public void run() {
//
//            int cnt = 1;
//            while (isPause()) {
//                if (isMessageClear()) {
//                    LOGGER.info("---ALL CONSUMERS IS PAUSE---");
//                    LOGGER.info("---ALL CONSUMERS IS PAUSE---");
//                    LOGGER.info("---ALL CONSUMERS IS PAUSE---");
//                    return;
//                }
//                LOGGER.info("SelfInspection will start at [" + cnt + "] seconds later");
//                sleep(cnt);
//                cnt++;
//            }
//
//        }
//
//    }
//
//    private static void sleep(int second) {
//
//        try {
//            Thread.sleep(second * 1000);
//        }
//        catch (InterruptedException e) {
//            LOGGER.error("thread sleep error:", e);
//        }
//    }
//
//    public static void pause() {
//
//        if (FENCE.compareAndSet(true, false)) {
//            LOGGER.info("TRY TO PAUSE ALL CONSUMERS");
//            EXEC.execute(new SelfInspection());
//        }
//        else {
//            LOGGER.info("PAUSE ALL CONSUMERS ALREADY!!!");
//        }
//    }
//
//    public static void resume() {
//
//        if (FENCE.compareAndSet(false, true)) {
//            LOGGER.info("TRY TO RESUME ALL CONSUMERS");
//            synchronized (GUARD) {
//                GUARD.notifyAll();
//            }
//        }
//        else {
//            LOGGER.info("RESUME ALL CONSUMERS ALREADY!!!");
//        }
//    }
//
//    public static boolean isPause() {
//
//        return FENCE.get() == false;
//    }
//
//    public static void initContent(String queueName, LinkedBlockingQueue<Runnable> instance) {
//
//        if (CONTENT.get(queueName) == null) {
//            CONTENT.putIfAbsent(queueName, instance);
//        }
//    }
//
//    public static void checkBarrier() {
//
//        if (isPause()) {
//            synchronized (GUARD) {
//                try {
//                    GUARD.wait();
//                }
//                catch (InterruptedException e) {
//                    LOGGER.error("object wait error:", e);
//                }
//            }
//        }
//    }
//}
