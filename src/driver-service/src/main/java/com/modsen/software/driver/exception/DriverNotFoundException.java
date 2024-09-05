package com.modsen.software.driver.exception;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException() {
        super("Requested driver doesn't exist");
    }
}
