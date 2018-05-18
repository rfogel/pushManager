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

import br.com.imusica.mongodbProvider.exception.MongoDBException;
import br.com.imusica.pushManager.api.GoogleAPI;
import br.com.imusica.pushManager.dao.CampaignDao;
import br.com.imusica.pushManager.executor.Executor;
import br.com.imusica.pushManager.model.AndroidPushNotification;
import br.com.imusica.pushManager.model.Campaign;
import br.com.imusica.pushManager.model.PushParameter;
import br.com.imusica.pushManager.model.Campaign.CampaignStatus;
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
public class CampaignPushNotificationJob extends GenericJob
{	
	private final static Logger logger = Logger.getLogger(Executor.class.getSimpleName());
	
	@Autowired
	@Qualifier("campaignProvider")
	private MQProvider mqProvider;
	
	@Autowired
	private CampaignDao campaignDao;
	
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
		
		final String campaignId = getJobData(CampaignModel.ID);
		
		final String queueName = getJobData(CampaignModel.CAMPAIGN_NAME);
		
		final Integer batch = getJobDataAsInteger(CampaignModel.BATCH);
		
		final String consumerId = "id_" + queueName;
		
		Campaign campaign = null;
		
		logger.info(String.format("CampaignPushNotificationJob starting for queue %s",queueName));
		
		try {
			
			campaign = campaignDao.findCampaign(new Campaign(campaignId));
			
			campaign.setStatus(CampaignStatus.RUNNING);
			
			campaignDao.updateCampaign(campaign);
			
			CampaignPushNotificationConsumer campaignPushNotificationConsumer = null;
			
			while ( mqProvider.getMessageCount(queueName) > 0 )
			{
				campaignPushNotificationConsumer = new CampaignPushNotificationConsumer(campaign);
				
				mqProvider.registerListener(consumerId, campaignPushNotificationConsumer);
				
				mqProvider.startBlockingConsumer(queueName, consumerId, Listener.MANUAL_ACK, batch);
				
				try {
					campaignPushNotificationConsumer.flushCampaign();			
				} catch (IOException e) {
					getLogger().error(ExceptionUtil.getStackTrace(e));
					
					List<String> messages = new ArrayList<>();
					
					for ( PushNotification pushNotification : campaignPushNotificationConsumer.getCampaign().getNotifications() )
						messages.add(parser.toJson(pushNotification));
					
					mqProvider.publishMessage(messages, queueName);
					
					campaign.setStatus(CampaignStatus.RESCHEDULED);
					
					campaignDao.updateCampaign(campaign);		
					
					logger.info(String.format("CampaignPushNotificationJob rescheduling for queue %s",queueName));
					
					return;
				}		
			}
			
			campaign.setStatus(CampaignStatus.FINISHED);
			
			campaignDao.updateCampaign(campaign);
			
		} catch (MQException | MongoDBException e) {
			
			getLogger().error(ExceptionUtil.getStackTrace(e));
						
			try {
				campaign.setStatus(CampaignStatus.RESCHEDULED);
				
				campaignDao.updateCampaign(campaign);
				
				logger.info(String.format("CampaignPushNotificationJob rescheduling for queue %s",queueName));
				
				return;
			} catch (MongoDBException e1) {
				getLogger().error(ExceptionUtil.getStackTrace(e1));
			}	
		}
		
		logger.info(String.format("CampaignPushNotificationJob finalizing for queue %s",queueName));
	}
	
	protected final class CampaignPushNotificationConsumer implements Listener
	{		
		private final static String MESSAGE_KEY = "message";
		
		private Campaign campaign; 
		
		public CampaignPushNotificationConsumer(Campaign campaign) {
			setCampaign(campaign);
		}
		
		public void flushCampaign() throws IOException 
		{
			List<String> devices = new ArrayList<>();
			Map<String,String> data = new HashMap<>();
			
			for (PushNotification pushNotification : campaign.getNotifications())	
				devices.add(pushNotification.getDeviceID());			
			
			if ( campaign.getParameters() != null )
				for (PushParameter pushParameter : campaign.getParameters())
					data.put(pushParameter.getKey(), pushParameter.getValue());
			
			data.put(MESSAGE_KEY,campaign.getMessage());
			
			googleAPI.sendPush(data,devices);
			
			campaign.getNotifications().clear();
		}
		
		@Override
		public void onMessage(MQMessage message) 
		{
			try
			{				
				PushNotification pushNotification = parser.fromJson(message.getMessage(), AndroidPushNotification.class);			
				
				getCampaign().addPushNotification(pushNotification);
				
				message.doAck();
				
			} catch (MQException e) {
				getLogger().error(ExceptionUtil.getStackTrace(e));
			}
		}

		public Campaign getCampaign() {
			return campaign;
		}

		public void setCampaign(Campaign campaign) {
			this.campaign = campaign;
		}
	}
}
