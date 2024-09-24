package com.modsen.software.passenger.exception;

public class DuplicatePhoneNumberException extends RuntimeException {
    public DuplicatePhoneNumberException() {
        super("Provided phone number is already in use by another passenger");
    }
}
