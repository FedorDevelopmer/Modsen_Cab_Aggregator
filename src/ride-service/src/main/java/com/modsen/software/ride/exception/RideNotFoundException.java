package com.modsen.software.ride.exception;


public class RideNotFoundException extends RuntimeException {
    public RideNotFoundException() {
        super("Requested ride doesn't exist");
    }
}
