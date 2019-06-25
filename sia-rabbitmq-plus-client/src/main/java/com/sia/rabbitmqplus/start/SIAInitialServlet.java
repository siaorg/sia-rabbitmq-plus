package com.sia.rabbitmqplus.start;

import com.sia.rabbitmqplus.binding.Consumer;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

/**
 * @author dingyi
 * @date 2014-8-11 下午2:48:04
 */
public class SIAInitialServlet extends HttpServlet {

    private static final long serialVersionUID = 8659061342981467598L;

    @Override
    public void init(ServletConfig config) {
        ServletContext sc = config.getServletContext();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(sc);
        Consumer.start(appContext);
    }
}
