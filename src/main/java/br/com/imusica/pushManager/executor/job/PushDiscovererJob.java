package br.com.imusica.pushManager.executor.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.imusica.mqProvider.MQProvider;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class PushDiscovererJob extends DiscoverJob
{	
	@Autowired
	@Qualifier("pushProvider")
	private MQProvider mqProvider;
	
	@Override
	protected MQProvider getProvider() {
		return mqProvider;
	}

	@Override
	protected String getVHost() {
		return "push";
	}

	@Override
	protected String getJobName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	protected Class<? extends GenericJob> getJobClass() {
		return PushNotificationJob.class;
	}
}

