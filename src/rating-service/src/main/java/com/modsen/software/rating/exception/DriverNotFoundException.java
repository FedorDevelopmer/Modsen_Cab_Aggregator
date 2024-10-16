package com.modsen.software.rating.exception;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException() {
        super("Requested driver doesn't exist");
    }
}
