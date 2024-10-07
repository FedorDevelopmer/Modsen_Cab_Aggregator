package com.modsen.software.ride.exception;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException() {
        super("Driver of the ride doesn't exist");
    }
}
