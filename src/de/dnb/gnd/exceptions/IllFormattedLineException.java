package de.dnb.gnd.exceptions;

public class IllFormattedLineException extends Exception {

  public IllFormattedLineException() {
    super();
  }

  public IllFormattedLineException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public IllFormattedLineException(final String message) {
    super(message);
  }

  public IllFormattedLineException(final Throwable cause) {
    super(cause);
  }

  /**
   * 
   */
  private static final long serialVersionUID = 2959874934019232535L;

}
