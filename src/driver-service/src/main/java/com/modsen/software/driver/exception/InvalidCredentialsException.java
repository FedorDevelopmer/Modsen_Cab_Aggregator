package com.modsen.software.driver.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Provided credentials are invalid for particular request.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
