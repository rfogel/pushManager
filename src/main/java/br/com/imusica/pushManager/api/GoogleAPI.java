package br.com.imusica.pushManager.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;

public class GoogleAPI
{
	private Sender sender;
	
	private Integer time2live;
	
	public GoogleAPI(String GCM_API_KEY, Integer time2live)
	{
		this.time2live = time2live;
		
		sender = new Sender(GCM_API_KEY);
	}
	
	public void sendPush(Map<String,String> data, List<String> devices) throws IOException
	{	
		Message message = new Message.Builder().timeToLive(time2live).delayWhileIdle(false).setData(data).build();
        
		sender.send(message, devices, 1);     
	}
	
	public static void main(String[] args) throws IOException 
	{
		Sender sender = new Sender("AIzaSyBL6IBXx5QejHWvElr5PWL88lHTw4UnNwA");
		
		List<String> devices = new ArrayList<String>();
		devices.add("00000000-40a3-b042-cd5a-3a9f13efec4ee5-0EwHCYbU:APA91bFLKUmj8y5AtZ8CFTZUZ06clzDP1fJ76yVMd7i7yXj34hGsnqPRObCCQsoVtKss0tLBd71Ipye4HugDhkCnQ1CGXx2-_Xmqbqki1sbnPAsZ1r4VdAopWij5lgIxR8FZHTCeJ6It");
		
		Map<String,String> data = new HashMap<>();
		
		String utm_source = "Telcel";
		String utm_medium = "Push Notification";
		String utm_term = "Assinaturas Novas";
		String utm_content = "Open App";
		String utm_campaign = "Novos Assinantes [Setembro/2016]";
		
		data.put("utm_source", utm_source);
		data.put("utm_medium", utm_medium);
		data.put("utm_term", utm_term);
		data.put("utm_content", utm_content);
		data.put("utm_campaign", utm_campaign);
		data.put("message","opa");
		
		Message message = new Message.Builder().timeToLive(30).delayWhileIdle(false).setData(data).build();
        
		sender.send(message, devices, 1);   	
	}
}
