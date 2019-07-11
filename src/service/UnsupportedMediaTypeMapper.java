package service;

import java.util.logging.Level;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import helpers.Log;
@Provider
public class UnsupportedMediaTypeMapper implements ExceptionMapper<NotSupportedException> {
	@Override
	public Response toResponse(NotSupportedException ex) {
		Log.log(Level.INFO, "Unsupported media type");
		return Response.status(415).entity(ex.getMessage()).type("text/plain").build();
	}
}