package de.dnb.gnd.exceptions;

public class IgnoringHandler implements ExceptionHandler {

	@Override
	public void handle(final Exception e, final String errorMessage) {
		//do nothing, just ignore the exception

	}

}
