package service;

import java.util.logging.Level;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import helpers.Log;
@Provider
public class NotAllowedMapper implements ExceptionMapper<NotAllowedException> {
	@Override
	public Response toResponse(NotAllowedException ex) {
		Log.log(Level.INFO, "Unsupported method");
		return Response.status(405).entity(ex.getMessage()).type("text/plain").build();
	}
}