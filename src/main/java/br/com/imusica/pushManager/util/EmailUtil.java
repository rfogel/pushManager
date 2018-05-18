package br.com.imusica.pushManager.util;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;

public final class EmailUtil
{		
	private static Session session;
	
	@Value("${email_user}")
	private String user;
	@Value("${email_password}")
	private String password;
	@Value("${email_recipients}")
	private String recipients;
	
	public EmailUtil() {}
	
	@PostConstruct
	public void setUp()
	{		
		if ( session == null )
		{
			Properties properties = new Properties();
			
			properties.put("mail.smtp.host", "smtp.gmail.com");
			properties.put("mail.smtp.socketFactory.port", "465");
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.port", "465");
			
			session = Session.getDefaultInstance(properties, new Authenticator() 
			{
				protected PasswordAuthentication getPasswordAuthentication() 
		        {
		        	return new PasswordAuthentication(user, password);
		        }
			}); 
		}
	}
	
	public void sendEmail(String subject, String text) throws Exception 
	{						
		Address[] toUser = InternetAddress.parse(recipients);  
		
		MimeMessage message = new MimeMessage(session);
		
        message.setRecipients(Message.RecipientType.TO, toUser);
        message.setFrom(new InternetAddress("noreplay@imusica.com.br", "PushManager"));
        message.setSubject(subject);
        message.setText(text, "utf-8", "html");
        
        Transport.send(message);
	}
}
