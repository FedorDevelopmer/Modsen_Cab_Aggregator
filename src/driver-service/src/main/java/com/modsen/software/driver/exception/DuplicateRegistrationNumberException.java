package com.modsen.software.driver.exception;

public class DuplicateRegistrationNumberException extends RuntimeException {
    public DuplicateRegistrationNumberException() {
        super("Provided registration number already belongs to another car");
    }
}
