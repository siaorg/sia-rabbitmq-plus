package com.sia.rabbitmqplus.log;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sia.rabbitmqplus.binding.Const;

import java.io.IOException;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xinliang on 16/8/11.
 */
public class SkyTrainLogger {

	private SkyTrainLogger() {

	}

	private static final Logger LOGGER_SELF = LoggerFactory.getLogger(SkyTrainLogger.class);

	private static final Map<String, org.apache.log4j.Logger> loggerFileMap = new ConcurrentHashMap<String, org.apache.log4j.Logger>();

	/**
	 * @param exchangeOrQueueName
	 * @return
	 */
	private synchronized static org.apache.log4j.Logger getLoggerFile(String exchangeOrQueueName) {
		if (!loggerFileMap.containsKey(exchangeOrQueueName)) {
			org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(exchangeOrQueueName);
			Layout layout = new PatternLayout("%d %p [%c] - %m%n");
			Appender appender = null;
			try {
				if (System.getProperty("SKYTRAIN_DAILY_LOG") != null) {
					appender = new DailyRollingFileAppender(layout,
							Const.SKYTRAIN_LOG_ROOT + exchangeOrQueueName + ".log", "'.'yyyy-MM-dd");
				} else {
					appender = new RollingFileAppender(layout, Const.SKYTRAIN_LOG_ROOT + exchangeOrQueueName + ".log");
					((RollingFileAppender) appender)
							.setMaximumFileSize(toFileSize(Const.SKYTRAIN_LOG_FILESIZE, maxFileSize));
					((RollingFileAppender) appender).setMaxBackupIndex(Const.SKYTRAIN_LOG_FILENUMS);
				}
			} catch (IOException ex) {
				LOGGER_SELF.error(Const.SIA_LOG_PREFIX, ex);
			}
			LOGGER.addAppender(appender);
			loggerFileMap.put(exchangeOrQueueName, LOGGER);
		}
		return loggerFileMap.get(exchangeOrQueueName);
	}

	/**
	 * @param exchangeOrQueueName
	 * @param message
	 */
	public static void log(String exchangeOrQueueName, Object message) {
		getLoggerFile(exchangeOrQueueName).info(message);
	}

	private final static long maxFileSize = 10 * 1024 * 1024;

	private static long toFileSize(String value, long dEfault) {
		if (value == null) {
			return dEfault;
		}

		String s = value.trim().toUpperCase();
		long multiplier = 1;
		int index;

		if ((index = s.indexOf("KB")) != -1) {
			multiplier = 1024;
			s = s.substring(0, index);
		} else if ((index = s.indexOf("MB")) != -1) {
			multiplier = 1024 * 1024;
			s = s.substring(0, index);
		} else if ((index = s.indexOf("GB")) != -1) {
			multiplier = 1024 * 1024 * 1024;
			s = s.substring(0, index);
		}
		if (s != null) {
			try {
				return Long.valueOf(s).longValue() * multiplier;
			} catch (NumberFormatException e) {
				LOGGER_SELF.error(Const.SIA_LOG_PREFIX + "[" + s + "] is not in proper int form.");
				LOGGER_SELF.error(Const.SIA_LOG_PREFIX + "[" + value + "] not in expected format.", e);
			}
		}
		return dEfault;
	}
}
