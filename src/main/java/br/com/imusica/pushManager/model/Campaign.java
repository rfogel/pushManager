package br.com.imusica.pushManager.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.com.imusica.mongodbProvider.model.MongoModel;

public class Campaign extends MongoModel implements Serializable
{
	private static final long serialVersionUID = 8551320652675074333L;
	
	private transient List<PushNotification> notifications;
	
	private List<DeviceInfo> filters;
	
	private List<PushParameter> parameters;

	private String message;
	
	private String name;

	@XmlJavaTypeAdapter(DateFormatterAdapter.class) 
	private Date startDate;
	
	@XmlJavaTypeAdapter(DateFormatterAdapter.class) 
	private Date endDate;
	
	private Integer batch;
	
	private String status;
	
	private Integer size;
	
	public Campaign() 
	{		
		setNotifications(new ArrayList<PushNotification>());
		
		this.batch = 1000;
		
		status = CampaignStatus.SCHEDULED;
	}
	
	public Campaign(String id) 
	{
		super(id);
	}

	public List<PushNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<PushNotification> notifications) {
		this.notifications = notifications;
	}
	
	public void addPushNotification(PushNotification pn) {
		notifications.add(pn);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Long getTime2live() {
		return endDate.getTime() - startDate.getTime();		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getBatch() {
		return batch;
	}

	public void setBatch(Integer batch) {
		this.batch = batch;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public String getFormattedStartDate() {
		return DateFormatterAdapter.dateFormat.format(startDate);
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
	
	public String getFormattedEndDate() {
		return DateFormatterAdapter.dateFormat.format(endDate);
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public List<DeviceInfo> getFilters() {
		return filters;
	}

	public void setFilters(List<DeviceInfo> filters) {
		this.filters = filters;
	}
	
	public List<PushParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<PushParameter> parameters) {
		this.parameters = parameters;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "Campaign [message=" + message + ", name=" + name + ", startDate=" + startDate + ", endDate=" + endDate + ", time2live=" + getTime2live() + ", batch=" + batch + "]";
	}
	
	public interface CampaignStatus
	{
		String SCHEDULED = "SCHEDULED";
		String RESCHEDULED = "RESCHEDULED";
		String RUNNING = "RUNNING";
		String FINISHED = "FINISHED";
	}
	
	private static class DateFormatterAdapter extends XmlAdapter<String, Date>
	{
		private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		@Override
		public Date unmarshal(String v) throws Exception {
			return dateFormat.parse(v);
		}
		
		@Override
		public String marshal(Date v) throws Exception {
			return null;
		}
	}

}
