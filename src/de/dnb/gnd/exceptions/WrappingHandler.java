package de.dnb.gnd.exceptions;

/**
 * Handler, der die Exception weiterreicht, also wirft.
 * @author Christian_2
 *
 */
public class WrappingHandler implements ExceptionHandler {

	@Override
	public final void handle(final Exception e, final String errorMessage) {
		throw new RuntimeException(errorMessage, e);

	}

}
