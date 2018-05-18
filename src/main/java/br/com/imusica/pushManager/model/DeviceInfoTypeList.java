package br.com.imusica.pushManager.model;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfoTypeList
{
	private List<String> deviceTypes;

	public List<String> getDeviceTypes() {
		return deviceTypes;
	}

	public void setDeviceTypes(List<String> deviceTypes) {
		this.deviceTypes = deviceTypes;
	}

	public DeviceInfoTypeList() {
		this.deviceTypes = new ArrayList<>();
	}
}

