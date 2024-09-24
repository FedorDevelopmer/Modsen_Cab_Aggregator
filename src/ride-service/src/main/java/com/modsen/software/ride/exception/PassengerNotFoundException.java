package com.modsen.software.ride.exception;

public class PassengerNotFoundException extends RuntimeException {
    public PassengerNotFoundException() {
        super("Passenger of the ride doesn't exist");
    }
}
