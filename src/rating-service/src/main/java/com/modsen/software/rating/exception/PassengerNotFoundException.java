package com.modsen.software.rating.exception;

public class PassengerNotFoundException extends RuntimeException {
    public PassengerNotFoundException() {
        super("Requested passenger doesn't exist");
    }
}
