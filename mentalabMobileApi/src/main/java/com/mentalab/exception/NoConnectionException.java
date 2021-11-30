package com.mentalab.exception;

public class NoConnectionException extends MentalabException {

  public NoConnectionException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }
}
