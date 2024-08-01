package de.dnb.gnd.exceptions;

import de.dnb.gnd.utils.RecordUtils;

public class LoggingHandler implements ExceptionHandler {

	@Override
	public final void handle(final Exception e, final String errorMessage) {
		RecordUtils.logError(errorMessage + " / " + e.getMessage());
	}

}
