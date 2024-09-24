package com.modsen.software.driver.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("Provided email already belongs to another driver");
    }
}
