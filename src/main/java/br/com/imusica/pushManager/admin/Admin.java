package br.com.imusica.pushManager.admin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

@Component
@Path("/")
public class Admin
{
	@GET
	@Path("/healthcheck")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getHealthCheck() {
		return Response.ok("1").build();
	}
}
