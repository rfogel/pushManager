package br.com.imusica.pushManager.executor;

import org.apache.log4j.Logger;

public abstract class Executor
{	
	protected final static Logger logger = Logger.getLogger(Executor.class.getSimpleName());

	public abstract void postConstruct();
}
