package de.devsurf.web.shell.exception;

public class ApiException extends RuntimeException {
	private static final long serialVersionUID = 3250984009928672523L;

	public ApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApiException(String message) {
		super(message);
	}

	public ApiException(Throwable cause) {
		super(cause);
	}	
}
