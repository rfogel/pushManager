package br.com.imusica.pushManager.server;

import java.io.File;

import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.webapp.WebAppContext;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Server
{
	@Parameter(names = {"--war","-w"}, description="Web archive for deploy", required = true)
	private String warFile;
	@Parameter(names = {"--port","-p"}, description="Server port", required = true)
	private Integer port;
	
	public static void main(String[] args) throws Exception 
	{
		Server server = new Server();
		
		new JCommander(server,args);
		
		server.run();
	}
	
	public void run() throws Exception 
	{
		org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(port);
		
		WebAppContext webapp = new WebAppContext();
		
		webapp.setContextPath("/");
		
		File warFile = new File(this.warFile);
		webapp.setWar(warFile.getAbsolutePath());
		webapp.addAliasCheck(new AllowSymLinkAliasChecker());
		
		server.setHandler(webapp);
		server.start();
		server.join();
	}
	
}
