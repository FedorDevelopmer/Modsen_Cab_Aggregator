package com.modsen.software.passenger.exception_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
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
