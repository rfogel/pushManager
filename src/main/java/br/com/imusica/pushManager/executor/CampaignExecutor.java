package br.com.imusica.pushManager.executor;

import javax.annotation.PostConstruct;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.imusica.pushManager.executor.job.CampaignDiscovererJob;
import br.com.imusica.pushManager.executor.job.PushDiscovererJob;
import br.com.imusica.pushManager.util.ExceptionUtil;

@Component
public class CampaignExecutor extends Executor
{	
	@Autowired
	private Scheduler scheduler;
	
	public CampaignExecutor() {
	}
	
	@Override
	@PostConstruct
	public void postConstruct()
	{
		logger.info("Starting CampaignExecutor");
				
		try
		{		
			scheduler.start();
			
			JobDetail campaignDiscovererJob = JobBuilder.newJob(CampaignDiscovererJob.class)
					.withIdentity(CampaignDiscovererJob.class.getSimpleName())
					.build();
			
			JobDetail pushDiscovererJob = JobBuilder.newJob(PushDiscovererJob.class)
					.withIdentity(PushDiscovererJob.class.getSimpleName())
					.build();
			
			Trigger campaignDiscovererJobTrigger = TriggerBuilder.newTrigger()
					.withIdentity(CampaignDiscovererJob.class.getSimpleName())
					.withSchedule(CronScheduleBuilder.cronSchedule("0/30 * * * * ?"))
					.build();
			
			Trigger pushDiscovererJobTrigger = TriggerBuilder.newTrigger()
					.withIdentity(PushDiscovererJob.class.getSimpleName())
					.withSchedule(CronScheduleBuilder.cronSchedule("0/30 * * * * ?"))
					.build();
			
			scheduler.scheduleJob(campaignDiscovererJob, campaignDiscovererJobTrigger);
			scheduler.scheduleJob(pushDiscovererJob, pushDiscovererJobTrigger);
			
		} catch (SchedulerException e) {
			logger.error(ExceptionUtil.getStackTrace(e));
		}
	}
}
