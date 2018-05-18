package br.com.imusica.pushManager.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imusica.mqProvider.MQException;
import com.imusica.mqProvider.MQProviderAPI;
import com.imusica.mqProvider.rabbitmq.api.Queue;
import com.imusica.restProvider.RestProvider;
import com.imusica.restProvider.entity.HeaderEntity;
import com.imusica.restProvider.exception.RestException;
import com.imusica.restProvider.response.RestResponse;

public class RabbitAPI implements MQProviderAPI
{
	final Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Autowired
	private RestProvider restProvider;
	
	@Autowired 
	private Gson parser;
	
	private HeaderEntity authHeader;
	
	private String host;
	
	public RabbitAPI(String host, String username, String password)
	{
		this.host = host;

		authHeader = HeaderEntity.getPrototype();
		authHeader.add("Authorization", "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary((username + ":" + password).getBytes()));
	}
	
	@Override
	public List<Queue> getQueues() throws MQException
	{
		String endpoint = String.format("%s/queues",host);
		
		try {
			RestResponse response = restProvider.doGet(endpoint,authHeader);			
			return parser.fromJson(response.getBody(), new TypeToken<ArrayList<Queue>>() {}.getType());			
		} catch (RestException e) {
			throw new MQException(e);
		}
	}
	
	@Override
	public List<Queue> getQueuesByVhost(String vhost) throws MQException
	{
		String endpoint = String.format("%s/queues/%s",host,vhost);
		
		try {
			RestResponse response = restProvider.doGet(endpoint,authHeader);			
			return parser.fromJson(response.getBody(), new TypeToken<ArrayList<Queue>>() {}.getType());			
		} catch (RestException e) {
			throw new MQException(e);
		}
	}
	
	@Override
	public Queue getQueueByName(String name, String vhost) throws MQException
	{		
		String endpoint = String.format("%s/queues/%s/%s",host,vhost,name);
		
		try {
			RestResponse response = restProvider.doGet(endpoint,authHeader);		
			return parser.fromJson(response.getBody(), Queue.class);
		} catch (RestException e) {
			throw new MQException(e);
		}
	}
}
