package de.devsurf.web.shell;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sun.jersey.spi.resource.Singleton;
import com.wordnik.swagger.core.Api;

@Path("/shell.json")
@Api(value = "/shell", 
	description = "Shell operations")
@Singleton
@Produces({ "application/json" })
public class JSONShellResource extends ShellResource {
}
