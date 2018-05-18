package br.com.imusica.pushManager.executor.job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.imusica.pushManager.executor.Executor;
import br.com.imusica.pushManager.model.CampaignModel;
import br.com.imusica.pushManager.util.ExceptionUtil;
import br.com.imusica.pushManager.util.StringUtil;

import com.imusica.mqProvider.MQException;
import com.imusica.mqProvider.MQProvider;
import com.imusica.mqProvider.MQProviderAPI;
import com.imusica.mqProvider.rabbitmq.api.Queue;

public abstract class DiscoverJob extends GenericJob
{
	final Logger logger = Logger.getLogger(Executor.class.getSimpleName());
	
	@Autowired
	private MQProviderAPI providerAPI;
	
	@Autowired
	private Scheduler scheduler;
	
	protected abstract MQProvider getProvider();
	
	protected abstract String getVHost();
	
	protected abstract String getJobName();
	
	protected abstract Class<? extends GenericJob> getJobClass();
	
	@Override
	public Logger getLogger() {
		return logger;
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException 
	{
		setJobProperties(context);
		
		logger.debug(String.format("Starting %s",getJobName()));
		
		try
		{
			for ( Queue queue : providerAPI.getQueuesByVhost(getVHost()) )
			{				
				final String id = queue.getArguments().getId();
				
				final String name = queue.getName();
				
				final String startDate = queue.getArguments().getStartDate();
				
				final String endDate = queue.getArguments().getEndDate();
				
				final String message = queue.getArguments().getMessage();
				
				final Integer batch = queue.getArguments().getBatch();
								
				if ( !StringUtil.isNullOrEmpty(endDate) )
				{
					if ( new Date().compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endDate)) >= 0 )
					{
						logger.info(String.format("%s found %s. Finished at %s. Deleting...", getJobName(), name, endDate));
					
						getProvider().deleteQueue(name,true);
						
						continue;
					}
				}
				
				Date date = (!StringUtil.isNullOrEmpty(startDate)) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDate) : getCurrentTimestamp();
				
				JobDetail job = JobBuilder.newJob(getJobClass())
						.withIdentity(String.format("%s_%s", getVHost(),name))
						.usingJobData(CampaignModel.ID, id)
						.usingJobData(CampaignModel.CAMPAIGN_NAME, name)
						.usingJobData(CampaignModel.MESSAGE, message)
						.usingJobData(CampaignModel.BATCH, batch)
						.build();
				
				Trigger trigger = TriggerBuilder.newTrigger()
						.withIdentity(name)
						.startAt(date)
						.build();
				
				if ( !scheduler.checkExists(job.getKey()) ) {
					logger.info(String.format("%s found %s. Scheduling at %s", getJobName(), name, startDate));
					scheduler.scheduleJob(job, trigger);
				}
			}
			
		} catch (MQException | SchedulerException | ParseException e) {
			logger.error(ExceptionUtil.getStackTrace(e));
		}
	}
	
	protected Date getCurrentTimestamp() {
		return Calendar.getInstance().getTime();
	}
}
