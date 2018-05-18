package br.com.imusica.pushManager;

import org.springframework.stereotype.Component;

import io.sentry.Sentry;
import io.sentry.SentryClient;

@Component
public class SentryManager
{
	public static SentryClient setSentryInstance(String dsn, String environment)
	{
		Sentry.init(dsn);
		Sentry.getStoredClient().setEnvironment(environment);
		
		return null;
	}
}
