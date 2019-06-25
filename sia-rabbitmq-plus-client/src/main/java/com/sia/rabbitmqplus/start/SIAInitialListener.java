package com.sia.rabbitmqplus.start;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sia.rabbitmqplus.binding.Const;
import com.sia.rabbitmqplus.binding.Consumer;

/**
 * SIA配置启动监听器类
 *
 * @author Ma.Liang
 * @version 1.0
 * @Date 2014/09/15
 */
public class SIAInitialListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SIAInitialListener.class);

	public SIAInitialListener() {
	}

	/**
	 * SIA配置启动监听器类初始化方法
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(sc);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

			LOGGER.error(Const.SIA_LOG_PREFIX, e);

		}
		Consumer.start(appContext);
	}

	/**
	 *
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		throw new UnsupportedOperationException();
	}

}
