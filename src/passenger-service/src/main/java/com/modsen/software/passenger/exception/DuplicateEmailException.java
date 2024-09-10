package com.modsen.software.passenger.exception;

public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException() {
        super("Provided phone number is already in use by another passenger");
    }
}
