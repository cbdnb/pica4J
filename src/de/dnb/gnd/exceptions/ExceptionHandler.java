package de.dnb.gnd.exceptions;

public interface ExceptionHandler {
	void handle(Exception e, String errorMessage);
}
