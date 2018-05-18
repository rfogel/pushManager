package br.com.imusica.pushManager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.imusica.mongodbProvider.model.MongoModel;

public class Device extends MongoModel implements Serializable
{	
	private static final long serialVersionUID = 9136711227702189817L;
	
	private List<DeviceInfo> deviceInfo;
	
	public Device() {
		setDeviceInfo(new ArrayList<DeviceInfo>());
	}

	public List<DeviceInfo> getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(List<DeviceInfo> deviceInfo) {
		this.deviceInfo = deviceInfo;
	}
	
	public DeviceInfo getDeviceInfoByKey(String key) 
	{
		for (DeviceInfo deviceInfo : getDeviceInfo())
			if ( deviceInfo.getKey().equals(key) )
				return deviceInfo;
		return null;
	}
	
	public Boolean containsDeviceInfo(String key) 
	{
		for (DeviceInfo deviceInfo : getDeviceInfo())
			if ( deviceInfo.getKey().equals(key) )
				return true;
		return !true;
	}
}
