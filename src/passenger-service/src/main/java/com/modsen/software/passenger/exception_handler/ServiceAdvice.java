package com.modsen.software.passenger.exception_handler;

import com.modsen.software.passenger.exception.DuplicateEmailException;
import com.modsen.software.passenger.exception.DuplicatePhoneNumberException;
import com.modsen.software.passenger.exception.PassengerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class ServiceAdvice {

    @ExceptionHandler({PassengerNotFoundException.class, DuplicateEmailException.class, DuplicatePhoneNumberException.class})
    public ResponseEntity<String> handleDataException(RuntimeException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        if (e instanceof DuplicateEmailException || e instanceof DuplicatePhoneNumberException) {
            status = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(String.format("%s %s", getExceptionTime(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage()), status);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleBind(BindException e) {
        return new ResponseEntity<>(String.format("%s %s", getExceptionTime(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableData(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(String.format("%s %s", getExceptionTime(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleUnknownErrors(RuntimeException e) {
        return new ResponseEntity<>(String.format("%s %s", getExceptionTime(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getExceptionTime() {
        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
