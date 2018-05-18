package br.com.imusica.pushManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class ApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext)
	{
		Logger.getLogger("org.mongodb.driver.connection").setLevel(Level.OFF);
		Logger.getLogger("org.mongodb.driver.management").setLevel(Level.OFF);
		Logger.getLogger("org.mongodb.driver.cluster").setLevel(Level.OFF);
		Logger.getLogger("org.mongodb.driver.protocol.insert").setLevel(Level.OFF);
		Logger.getLogger("org.mongodb.driver.protocol.query").setLevel(Level.OFF);
		Logger.getLogger("org.mongodb.driver.protocol.update").setLevel(Level.OFF);
		
		Properties properties = new Properties();
		
		try {
			properties.load(new FileInputStream(System.getProperty("push.path") + "/application.properties"));
		} catch (IOException ex) {
			try {
				properties.load(ApplicationInitializer.class.getResourceAsStream("/application.properties"));
			} catch (IOException e) {
				System.exit(1);
			}
		}
				
		String activeProfile = properties.getProperty("profile","default");
		
		applicationContext.getEnvironment().setActiveProfiles(activeProfile);
	}	
}
