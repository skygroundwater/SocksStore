package com.socksstore.exceptions;

public class NotEnoughSocksException extends RuntimeException {

    public NotEnoughSocksException() {
        super();
    }

    @Override
    public String getMessage() {
        return "We don't have the correct number of socks in stock";
    }
}
