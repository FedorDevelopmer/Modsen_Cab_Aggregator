package com.modsen.software.passenger.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Provided credentials are invalid for particular request.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
