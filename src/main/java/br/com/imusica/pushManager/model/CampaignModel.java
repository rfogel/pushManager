package br.com.imusica.pushManager.model;

public interface CampaignModel
{
	String ID = "id";
	String CAMPAIGN_NAME = "queueName";
	String BATCH = "batch";
	String START_DATE = "startDate";
	String END_DATE = "endDate";
	String TIME2LIVE = "x-expires";
	String MESSAGE = "message"; 
	String PARAMETERS = "parameters";
}
