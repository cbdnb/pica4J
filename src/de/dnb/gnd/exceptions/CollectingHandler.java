package de.dnb.gnd.exceptions;

import java.util.ArrayList;
import java.util.List;

public class CollectingHandler implements ExceptionHandler {

  private final List<Exception> exceptions = new ArrayList<>();

  public final List<Exception> getExceptions() {
    return exceptions;
  }

  @Override
  public final void handle(final Exception e, final String errorMessage) {
    exceptions.add(e);

    //message is ignored here, but could have been
    //collected too.

  }

}
