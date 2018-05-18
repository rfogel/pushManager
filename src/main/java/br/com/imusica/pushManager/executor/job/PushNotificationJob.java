package br.com.imusica.pushManager.executor.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import br.com.imusica.pushManager.api.GoogleAPI;
import br.com.imusica.pushManager.executor.Executor;
import br.com.imusica.pushManager.model.CampaignModel;
import br.com.imusica.pushManager.model.PushNotification;
import br.com.imusica.pushManager.util.ExceptionUtil;

import com.google.gson.Gson;
import com.imusica.mqProvider.Listener;
import com.imusica.mqProvider.MQException;
import com.imusica.mqProvider.MQMessage;
import com.imusica.mqProvider.MQProvider;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class PushNotificationJob extends GenericJob
{	
	private final static Logger logger = Logger.getLogger(Executor.class.getSimpleName());
	
	@Autowired
	@Qualifier("pushProvider")
	private MQProvider mqProvider;
	
	@Autowired
	private GoogleAPI googleAPI;
	
	@Autowired 
	private Gson parser;
	
	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException 
	{
		setJobProperties(context);
		
		final String queueName = getJobData(CampaignModel.CAMPAIGN_NAME);
		
		final String consumerId = "id_" + queueName;
		
		logger.info(String.format("PushNotificationJob starting for queue %s",queueName));
		
		try {
			
			mqProvider.registerListener(consumerId, new PushNotificationConsumer());
			mqProvider.startBlockingConsumer(queueName, consumerId, Listener.MANUAL_ACK);
			
		} catch (MQException e) {
			getLogger().error(ExceptionUtil.getStackTrace(e));
		}
		
		logger.info(String.format("PushNotificationJob finalizing for queue %s",queueName));
	}
	
	protected final class PushNotificationConsumer implements Listener
	{	
		private final static String MESSAGE_KEY = "message";
		
		@Override
		public void onMessage(MQMessage message) 
		{
			try
			{				
				PushNotification pushNotification = parser.fromJson(message.getMessage(), PushNotification.class);
				
				List<String> devices = new ArrayList<>();
				
				devices.add(pushNotification.getDeviceID());	
				
				Map<String,String> data = new HashMap<>();
				
				data.put(MESSAGE_KEY,pushNotification.getMessage());
				
				googleAPI.sendPush(data,devices);
				
				message.doAck();
				
			} catch (MQException | IOException e) {
				getLogger().error(ExceptionUtil.getStackTrace(e));
			}
		}
	}
}
