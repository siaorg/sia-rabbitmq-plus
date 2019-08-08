package com.sia.rabbitmqplus.log;

import com.sia.rabbitmqplus.binding.FileSize;
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
 * @author xinliang on 16/8/11.
 */
public class SkyTrainLogger {

	private SkyTrainLogger() {

	}

	private static final Logger LOGGER_SELF = LoggerFactory.getLogger(SkyTrainLogger.class);

	private static final Map<String, org.apache.log4j.Logger> LOGGER_FILE_MAP = new ConcurrentHashMap<String, org.apache.log4j.Logger>();


	/**
	 * @param exchangeOrQueueName
	 * @return
	 */
	private synchronized static org.apache.log4j.Logger getLoggerFile(String exchangeOrQueueName) {
		if (!LOGGER_FILE_MAP.containsKey(exchangeOrQueueName)) {
			org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(exchangeOrQueueName);
			Layout layout = new PatternLayout("%d %p [%c] - %m%n");
			Appender appender = null;
			try {
				if (System.getProperty(Const.SKYTRAIN_DAILY_LOG) != null) {
					appender = new DailyRollingFileAppender(layout,
							Const.SKYTRAIN_LOG_ROOT + exchangeOrQueueName + ".log", "'.'yyyy-MM-dd");
				} else {
					appender = new RollingFileAppender(layout, Const.SKYTRAIN_LOG_ROOT + exchangeOrQueueName + ".log");
					((RollingFileAppender) appender)
							.setMaximumFileSize(toFileSize(Const.SKYTRAIN_LOG_FILESIZE, MAX_FILE_SIZE));
					((RollingFileAppender) appender).setMaxBackupIndex(Const.SKYTRAIN_LOG_FILENUMS);
				}
			} catch (IOException ex) {
				LOGGER_SELF.error(Const.SIA_LOG_PREFIX, ex);
			}
			logger.addAppender(appender);
			LOGGER_FILE_MAP.put(exchangeOrQueueName, logger);
		}
		return LOGGER_FILE_MAP.get(exchangeOrQueueName);
	}

	/**
	 * @param exchangeOrQueueName
	 * @param message
	 */
	public static void log(String exchangeOrQueueName, Object message) {
		getLoggerFile(exchangeOrQueueName).info(message);
	}

	private final static long MAX_FILE_SIZE = 10 * 1024 * 1024;

	private static long toFileSize(String value, long dEfault) {
		if (value == null) {
			return dEfault;
		}

		String s = value.trim().toUpperCase();
		long multiplier = 1;
		int index;

		if ((index = s.indexOf(FileSize.KB)) != -1) {
			multiplier = 1024;
			s = s.substring(0, index);
		} else if ((index = s.indexOf(FileSize.MB)) != -1) {
			multiplier = 1024 * 1024;
			s = s.substring(0, index);
		} else if ((index = s.indexOf(FileSize.GB)) != -1) {
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
