package br.com.imusica.pushManager.executor.job;

import org.apache.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.UnableToInterruptJobException;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public abstract class GenericJob implements Job, InterruptableJob
{
	private JobExecutionContext context;

	public abstract Logger getLogger();
	
	protected void setJobProperties(JobExecutionContext context) {
		this.context = context;
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	protected String getJobData(String key) {
		try {
			if ( context.getJobDetail().getJobDataMap().containsKey(key) )
				return context.getJobDetail().getJobDataMap().getString(key);
		} catch (ClassCastException e) {}
		return null;
	}
	
	protected Integer getJobDataAsInteger(String key) {
		try {
			if ( context.getJobDetail().getJobDataMap().containsKey(key) )
				return context.getJobDetail().getJobDataMap().getIntValue(key);
		} catch (ClassCastException e) {}
		return null;
	}
	
	protected Boolean getJobDataAsBoolean(String key) {
		try {
			if ( context.getJobDetail().getJobDataMap().containsKey(key) )
				return context.getJobDetail().getJobDataMap().getBooleanValue(key);
		} catch (ClassCastException e) {}
		return null;
	} 
	
	@Override
	public void interrupt() throws UnableToInterruptJobException {
		getLogger().warn("Interruption JOB " + context.getJobDetail().getKey().getName() + " via trigger " + context.getTrigger().getKey().getName() + " ...");
	}
}
