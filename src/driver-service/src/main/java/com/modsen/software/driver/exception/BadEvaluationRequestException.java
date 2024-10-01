package com.modsen.software.driver.exception;

public class BadEvaluationRequestException extends RuntimeException {
    public BadEvaluationRequestException() {
        super("Bad request for evaluation of mean rating for passenger");
    }
}
