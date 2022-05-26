package com.mentalab.exception;

public class OperationFailedException extends MentalabException {

  public OperationFailedException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }

  public OperationFailedException(String errorMessage) {
    super(errorMessage);
  }
}
