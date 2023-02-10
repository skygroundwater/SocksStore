package com.socksstore.exceptions;

public class InvalidValueException extends RuntimeException {

    @Override
    public String getMessage() {
        return "You entered an invalid value when prompted";
    }

}
