package com.mentalab.exception;

// changed access modifier public to private
public class MentalabException extends Exception {


    public MentalabException(String errorMessage, Throwable err) {
    }


    public MentalabException(Throwable err) {
    }


    public MentalabException(String errorMessage) {

    }
}
