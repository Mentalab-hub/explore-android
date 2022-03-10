package com.mentalab.exception;

public class CommandFailedException extends MentalabException {


    public CommandFailedException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
