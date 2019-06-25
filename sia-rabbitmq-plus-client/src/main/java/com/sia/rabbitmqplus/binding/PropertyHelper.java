package com.sia.rabbitmqplus.binding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHelper.class);
	private static String currentPath = null;

	private PropertyHelper() {

	}

	private static String getAbsoluteFilePath(String file) {
		if (getCurrentPath().endsWith(File.separator))
			return getCurrentPath() + file;
		else
			return getCurrentPath() + File.separator + file;
	}

	protected static InputStream loadFile(String file) {

		if (isEmpty(file)) {
			return null;
		}
		InputStream fip = null;

		String cfgFilePath = getAbsoluteFilePath(file);
		File cfgFile = new File(cfgFilePath);
		if (cfgFile.exists()) {
			try {
				fip = new FileInputStream(cfgFile);
			} catch (FileNotFoundException e) {
				LOGGER.error(Const.SIA_LOG_PREFIX, e);
			}
		} else {
			LOGGER.error(Const.SIA_LOG_PREFIX + "[读取配置文件失败，请检查文件是否存在!] [" + cfgFilePath + "]");
		}
		return fip;
	}

	protected static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	protected static String getCurrentPath() {
		if (currentPath != null) {
			return currentPath;
		} else if (System.getProperty("SKYTRAIN_FILE_PATH") != null) {
			return System.getProperty("SKYTRAIN_FILE_PATH");
		} else {
			File temp = new File("");
			return temp.getAbsolutePath();
		}
	}

	protected static Properties load(String file) {

		InputStream in = PropertyHelper.class.getClassLoader().getResourceAsStream(file);
		if (in == null)
			in = ClassLoader.getSystemResourceAsStream(file);
		if (in == null)
			in = PropertyHelper.loadFile(file);
		if (in == null) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "[读取配置文件失败，请将<" + file + ">放置在[" + PropertyHelper.getCurrentPath()
					+ "]路径下]");
			System.err.println(Const.SIA_LOG_PREFIX + "[读取配置文件失败，请将<" + file + ">放置在[" + PropertyHelper.getCurrentPath()
					+ "]路径下]");
		}

		Properties prop = new Properties();
		try {
			prop.load(in);
		} catch (IOException e) {
			LOGGER.error(Const.SIA_LOG_PREFIX + "[读取配置文件失败，请检查路径与文件名]-->" + file, e);
		}
		return prop;
	}

	public static void setProfilePath(String path) {
		currentPath = path;
	}
}
