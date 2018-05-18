package br.com.imusica.pushManager.test;

import java.io.IOException;
import java.util.ArrayList;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

public class PushTest
{
	public static void main(String[] args) throws IOException 
	{
		System.setProperty("path","C:/Users/rafael.fogel/Desktop/Workspace/rbtServices/src/main/resources/dev");
		System.setProperty("log.path","C:/Users/rafael.fogel/Desktop/Workspace/rbtServices/src/main/resources/dev/log");
		
		String GCM_API_KEY = "AIzaSyBL6IBXx5QejHWvElr5PWL88lHTw4UnNwA";
		String REG_ID = "estRaiS7-r8:APA91bHglhTZJhLAi4iY-ViaJxFGPhhFelS8DUNYobFf29TamsIiwHyT8g7Tq0UEqb8SnHl3LRDEX3YO73UNLhTT_8LZOqTdHN2bmK-6jJSeegp54BwhHh7YORs4gjqA2F3xdLK9P06m";
		String MESSAGE_KEY = "message";
		String MESSAGE_VALUE = "Bolsomito";
		
		Sender sender = new Sender(GCM_API_KEY);

        ArrayList<String> devicesList = new ArrayList<String>();
		devicesList.add(REG_ID);

		Message message = new Message.Builder().timeToLive(30).delayWhileIdle(true).addData(MESSAGE_KEY, MESSAGE_VALUE).build();
        MulticastResult result = sender.send(message, devicesList, 1);
        sender.send(message, devicesList, 1);
        System.out.println(result.toString());
	}
}
