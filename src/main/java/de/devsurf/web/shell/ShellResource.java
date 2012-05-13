package de.devsurf.web.shell;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.spi.ValueCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.standalone.Bootstrap;
import org.crsh.util.Strings;

import com.google.gson.Gson;
import com.wordnik.swagger.core.ApiError;
import com.wordnik.swagger.core.ApiErrors;
import com.wordnik.swagger.core.ApiOperation;
import com.wordnik.swagger.core.ApiParam;
import com.wordnik.swagger.jaxrs.JavaHelp;

import de.devsurf.web.shell.exception.ApiException;

public class ShellResource extends JavaHelp {
//	@Inject
	private Bootstrap bootstrap;

//	@Inject
	private CRaSH crash;

//	@Inject
	private Shell shell;

	private CommandExecution exec;

	@PostConstruct
	public void init() {
		System.out.println("Starting");
		try {
			bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader());
			bootstrap.bootstrap();
			crash = new CRaSH(bootstrap.getContext());
			shell = crash.createSession(null);
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}
		System.out.println("Started");
	}

	@GET
	@Path("/welcome")
	@ApiOperation(value = "Returns the welcome message of the console.", notes = "Returns the welcome message.")
	@Produces("text/html")
	public Response welcome() {
		return Response.ok(shell.getWelcome()).build();
	}

	@GET
	@Path("/execute")
	@ApiOperation(value = "Executes the given commandline.")
	@ApiErrors(value = { @ApiError(code = 506, reason = "No command was found.") })
	@Produces("text/html")
	public Response execute(
			@ApiParam(value = "The command which should be executed..", required = true) @QueryParam("line") String line) {
		ShellProcess process;
		try {
			process = shell.createProcess(line);
			CommandExecution execution = new CommandExecution(process);
			ShellResponse response = execution.execute();

			if (response != null) {
				return Response.ok(response.getText()).build();
			}
		} catch (Throwable e) {
			throw new ApiException(e);
		}
		return Response.ok().build();
	}

	@GET
	@Path("/cancel")
	@ApiOperation(value = "Cancel a running commandline execution", notes = "Returns nothing.")
	@ApiErrors(value = { @ApiError(code = 404, reason = "No command was found.") })
	public Response cancel() {
		// TODO find exec in session
		if (exec != null) {
			exec.cancel();
			return Response.ok().build();
		}
		return Response.status(404).build();
	}

	@GET
	@Path("/complete")
	@ApiOperation(value = "Completes the given command.", notes = "Returns the complete command.")
	@ApiErrors(value = { @ApiError(code = 505, reason = "Command couldn't be parsed.") })
	public Response complete(
			@ApiParam(value = "Prefix for the command.", required = true) @QueryParam("prefix") String prefix) {
		CommandCompletion completion = shell.complete(prefix);
		ValueCompletion completions = completion.getValue();
		List<String> values = new ArrayList<String>();
		if (completions.getSize() == 0) {
			// Do nothing
		} else {
			try {
				Delimiter delimiter = completion.getDelimiter();
				StringBuilder sb = new StringBuilder();
				if (completions.getSize() == 1) {
					String suffix = completions.getSuffixes().iterator().next();
					delimiter.escape(suffix, sb);
					if ((Boolean) completions.get(suffix)) {
						sb.append(delimiter.getValue());
					}
					values.add(sb.toString());
				} else {
					String commonCompletion = Strings
							.findLongestCommonPrefix(completions.getSuffixes());
					if (commonCompletion.length() > 0) {
						delimiter.escape(commonCompletion, sb);
						values.add(sb.toString());
					} else {
						String completionPrefix = completions.getPrefix();
						for (Map.Entry<String, Boolean> entry : completions) {
							sb.append(completionPrefix);
							delimiter.escape(entry.getKey(), sb);
							values.add(sb.toString());
							sb.setLength(0);
						}
					}
				}
			} catch (IOException e) {
				return Response.status(505).build();
			}
		}

		return Response.ok(new Gson().toJson(values)).build();
	}
}
