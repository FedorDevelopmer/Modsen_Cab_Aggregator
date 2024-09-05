package com.modsen.software.driver.exception;


public class CarNotFoundException extends RuntimeException{
    public CarNotFoundException(String message) {
        super(message);
    }
}
