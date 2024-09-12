package com.modsen.software.passenger.exception;


public class PassengerNotFoundException extends RuntimeException {
    public PassengerNotFoundException() {
        super("Requested passenger doesn't exist");
    }
}
