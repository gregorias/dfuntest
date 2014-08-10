package org.nebulostore.dfuntesting;

public class CommandException extends Exception {
  private static final long serialVersionUID = 1L;

  public CommandException() {
    super();
  }

  public CommandException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public CommandException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommandException(String message) {
    super(message);
  }

  public CommandException(Throwable cause) {
    super(cause);
  }
}
