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
import org.springframework.web.context.request.WebRequest;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


public class ExceptionHandling {
    public static ResponseEntity<Object> formExceptionResponse(HttpStatus exceptionStatus, Exception e, WebRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", exceptionStatus.value());
        responseBody.put("error", exceptionStatus.name());
        responseBody.put("message", e.getMessage());
        responseBody.put("path", request.getDescription(false));
        return new ResponseEntity<>(responseBody, exceptionStatus);
    }

}
