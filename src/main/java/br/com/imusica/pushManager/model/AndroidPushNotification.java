package br.com.imusica.pushManager.model;

import java.io.Serializable;
import java.util.List;

public class AndroidPushNotification extends PushNotification implements Serializable
{
	private static final long serialVersionUID = 838180956384361297L;
	
	private static final String GCM_ID = "gcmId";
	
	public AndroidPushNotification(String deviceID, String message, List<PushParameter> parameters) {
		super(deviceID, message, parameters);
		setPlatform(Platform.ANDROID);
	}
	
	public static String getPlatformID() {
		return GCM_ID;
	}
}
