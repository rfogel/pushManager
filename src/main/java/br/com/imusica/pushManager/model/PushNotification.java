package br.com.imusica.pushManager.model;

import java.io.Serializable;
import java.util.List;

public abstract class PushNotification implements Serializable
{
	private static final long serialVersionUID = 2100793152486370822L;

	private String title;
	
	private String message;
	
	private String deviceID;
	
	private Platform platform;
	
	private List<PushParameter> parameters;
	
	public PushNotification() {}
	
	public PushNotification(String deviceID, String message, List<PushParameter> parameters) {
		super();
		this.deviceID = deviceID;
		this.message = message;
		this.parameters = parameters;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<PushParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<PushParameter> parameters) {
		this.parameters = parameters;
	}

	public Platform getPlatform() {
		return platform;
	}

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}
	
	public enum Platform
	{
		ANDROID;
	}
}
