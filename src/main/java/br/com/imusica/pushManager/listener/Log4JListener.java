package br.com.imusica.pushManager.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;

public class Log4JListener implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent sce) 
	{
		if ( System.getProperty("push.path") != null )
			PropertyConfigurator.configure(System.getProperty("push.path") + "/log4j.properties");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {}
}
