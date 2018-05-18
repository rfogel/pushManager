package br.com.imusica.pushManager.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import br.com.imusica.mongodbProvider.exception.MongoDBException;
import br.com.imusica.pushManager.dao.CampaignDao;
import br.com.imusica.pushManager.dao.DeviceDao;
import br.com.imusica.pushManager.dao.DeviceInfoDao;
import br.com.imusica.pushManager.dao.PushParameterDao;
import br.com.imusica.pushManager.model.AndroidPushNotification;
import br.com.imusica.pushManager.model.Campaign;
import br.com.imusica.pushManager.model.CampaignModel;
import br.com.imusica.pushManager.model.Device;
import br.com.imusica.pushManager.model.PushNotification;
import br.com.imusica.pushManager.util.EmailUtil;
import br.com.imusica.pushManager.util.ExceptionUtil;
import io.sentry.Sentry;

import com.google.gson.Gson;
import com.imusica.mqProvider.MQException;
import com.imusica.mqProvider.MQMessage;
import com.imusica.mqProvider.MQProvider;
import com.imusica.mqProvider.MQProviderAPI;

@Component
@Path("/")
public class Manager
{	
	private final static int DEFAULT_QUERY_SIZE = 20000;
	
	protected final static Logger logger = Logger.getLogger(Manager.class.getSimpleName());
	
	private StringBuilder emailContent; 
	
	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private CampaignDao campaignDao;
	
	@Autowired
	private DeviceInfoDao deviceInfoDao;
	
	@Autowired
	private PushParameterDao pushParameterDao;
	
	@Autowired
	@Qualifier("pushProvider")
	private MQProvider pushProvider;
	
	@Autowired
	@Qualifier("campaignProvider")
	private MQProvider campaignProvider;
	
	@Autowired
	private MQProviderAPI providerAPI;
	
	@Autowired
	private Gson parser;
	
	@Autowired
	private EmailUtil emailUtil;
	
	@PostConstruct
	public void setUp()
	{
		emailContent = new StringBuilder();
		
		emailContent.append("Push start date: %s").append("<br>")
					.append("Push end date: %s").append("<br>")
					.append("Devices hitted: %s").append("<br>")
					.append("Parameters: %s").append("<br>")
					.append("Filters: %s");
	}
	
	@GET
	@Path("/device/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDevices()
	{	
		return Response.ok().entity(parser.toJson(deviceDao.findAllDevices())).build();
	}
	
	@GET
	@Path("/deviceInfo/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeviceInfoTypes()
	{	
		return Response.ok().entity(parser.toJson(deviceInfoDao.findAllDeviceInfoTypes())).build();
	}
	
	@GET
	@Path("/pushParameters/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPushParameters()
	{	
		return Response.ok().entity(parser.toJson(pushParameterDao.findAllPushParameters())).build();
	}
	
	@POST
	@Path("/push")
	public Response enqueuePushNotification(String message)
	{		
		try
		{
			pushProvider.createQueue(null);
			
			List<String> messages = new ArrayList<>();
			
			for ( Device device : deviceDao.findAllDevices() )
			{
				PushNotification push = new AndroidPushNotification(device.getDeviceInfoByKey(AndroidPushNotification.getPlatformID()).getValue(), message, null);
				messages.add(parser.toJson(push));
			}
			
			pushProvider.publishMessage(messages, "push", MQMessage.PERSISTENT);		
			
		} catch (MQException e) {
			logger.error(ExceptionUtil.getStackTrace(e));
		}
		
		return Response.ok().build();
	}
	
	@POST
	@Path("/campaign")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response enqueueCampaignByFilter(@Suspended final AsyncResponse asyncResponse, Campaign campaign)
	{		
		logger.info(String.format("Starting campaign %s", campaign));
		
		try
		{
			if ( campaignProvider.queueExists(campaign.getName()) )
				return Response.status(Status.PRECONDITION_FAILED).entity("Campaign already exists").build();
			if ( campaign.getTime2live() <= 0 )
				return Response.status(Status.PRECONDITION_FAILED).entity("Period incorrect").build();
			if ( campaign.getEndDate().before(new Date()) )
				return Response.status(Status.PRECONDITION_FAILED).entity("Invalid end date").build();
			if ( campaign.getFilters() == null || campaign.getFilters().size() == 0 )
				return Response.status(Status.PRECONDITION_FAILED).entity("No campaigns without filters are allowed").build();
			
			campaignDao.saveCampaign(campaign);
		
		} catch (Exception e) {
			logger.error(ExceptionUtil.getStackTrace(e));
			return Response.serverError().build();
		}
		
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				enqueueCampaign(campaign);
				asyncResponse.resume(true);
			}
			
		}).start();
		
		return Response.ok().build();
	}
	
	protected void enqueueCampaign(Campaign campaign)
	{
		logger.info(String.format("Enqueueing campaign %s", campaign));
		
		try
		{
			Map<String,Object> args = new HashMap<>();
			
			args.put(CampaignModel.ID, campaign.getIdentity());
			args.put(CampaignModel.START_DATE, campaign.getFormattedStartDate());
			args.put(CampaignModel.END_DATE, campaign.getFormattedEndDate());
			args.put(CampaignModel.BATCH, campaign.getBatch());
			args.put(CampaignModel.MESSAGE, campaign.getMessage());
			args.put(CampaignModel.PARAMETERS, parser.toJson(campaign.getParameters()));
			
			campaignProvider.createQueue(campaign.getName(), MQProvider.DURABLE, MQProvider.NOT_EXCLUSIVE, MQProvider.MANUAL_DELETE, args);
			
			int size = 0;
			
			boolean lerigou = true;
			
			Device reference = null; 
			
			List<Device> devices = new ArrayList<>();
									
			while ( lerigou )
			{
				reference = deviceDao.findDevicesByDeviceInfo(devices, campaign.getFilters(), DEFAULT_QUERY_SIZE, reference);
				
				List<String> messages = new ArrayList<>();
				
				for ( Device device : devices )
					if ( device.containsDeviceInfo(AndroidPushNotification.getPlatformID()))
						messages.add(parser.toJson(new AndroidPushNotification(device.getDeviceInfoByKey(AndroidPushNotification.getPlatformID()).getValue(), campaign.getMessage(), campaign.getParameters())));
				
				campaignProvider.publishMessage(messages, campaign.getName(), MQMessage.PERSISTENT);
				
				size += devices.size();
				
				lerigou = devices.size() == DEFAULT_QUERY_SIZE;
				
				devices.clear();
			}
					
			campaign.setSize(size);
						
			campaignDao.updateCampaign(campaign);
			
			String subject = String.format("Push scheduled to %s", campaign.getFormattedStartDate());
			
			String parameters = (campaign.getParameters() == null) ? null : campaign.getParameters().toString();
			String filters = (campaign.getFilters() == null) ? null : campaign.getFilters().toString();
			
			if ( size > 50 ){
				Sentry.capture(String.format(emailContent.toString(), campaign.getStartDate(),campaign.getEndDate(),campaign.getSize(),parameters,filters));
				emailUtil.sendEmail(subject, String.format(emailContent.toString(), campaign.getStartDate(),campaign.getEndDate(),campaign.getSize(),parameters,filters));
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtil.getStackTrace(e));
		}
	}
	
	@POST
	@Path("/campaign/preview")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response previewCampaignByFilter(Campaign campaign)
	{		
		logger.info("Starting campaign preview");
	
		return ( campaign.getFilters() != null && campaign.getFilters().size() > 0 ) ? Response.ok().entity(deviceDao.count(campaign.getFilters())).build() : Response.ok().entity(deviceDao.count()).build();	
	}
	
	@GET
	@Path("/campaign")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getCampaigns()
	{
		try {
			return Response.ok().entity(parser.toJson(campaignDao.findAllCampaign())).build();
		} catch (MongoDBException e) {
			logger.error(ExceptionUtil.getStackTrace(e));
		}
		
		return Response.ok().build();
	}
	
	@DELETE
	@Path("/campaign")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response deleteCampaign(Campaign campaign)
	{
		logger.info(String.format("Deleting campaign %s", campaign));
		
		try {
			campaignDao.deleteCampaign(campaign);
			campaignProvider.deleteQueue(campaign.getName(),true);
		} catch (MQException | MongoDBException e) {
			logger.error(ExceptionUtil.getStackTrace(e));
		}
		
		return Response.ok().build();
	}
}
