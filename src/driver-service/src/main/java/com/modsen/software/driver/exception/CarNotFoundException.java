package com.modsen.software.driver.exception;

public class CarNotFoundException extends RuntimeException {
    public CarNotFoundException() {
        super("Requested car doesn't exist");
    }
}
