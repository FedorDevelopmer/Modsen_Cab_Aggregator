package com.modsen.software.rating.exception;

public class RatingScoreNotFoundException extends RuntimeException {
    public RatingScoreNotFoundException() {
        super("Requested rating score doesn't exist");
    }
}
