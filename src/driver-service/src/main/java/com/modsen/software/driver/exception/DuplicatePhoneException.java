package com.modsen.software.driver.exception;

public class DuplicatePhoneException extends RuntimeException {
    public DuplicatePhoneException() {
        super("Provided phone number already belongs to another driver");
    }
}
