package de.devsurf.web.shell.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.crsh.command.NoSuchCommandException;

import de.devsurf.web.shell.model.ApiResponse;

@Provider
public class ShellExceptionMapper implements ExceptionMapper<ApiException> {
	public Response toResponse(ApiException exception) {
		if (exception.getCause() != null && exception.getCause() instanceof NoSuchCommandException) {
			return Response.status(Status.NOT_FOUND)
					.entity(new ApiResponse(ApiResponse.ERROR, exception
							.getMessage())).build();
		} else {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ApiResponse(ApiResponse.ERROR,
							"a system error occured")).build();
		}
	}
}