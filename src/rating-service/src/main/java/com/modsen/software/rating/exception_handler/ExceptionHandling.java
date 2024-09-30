package com.modsen.software.rating.exception_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import java.util.HashMap;
import java.util.Map;

public class ExceptionHandling {
    public static ResponseEntity<Object> formExceptionResponse(HttpStatus exceptionStatus, String message, WebRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", exceptionStatus.value());
        responseBody.put("error", exceptionStatus.name());
        responseBody.put("message", message);
        responseBody.put("path", request.getDescription(false));
        return new ResponseEntity<>(responseBody, exceptionStatus);
    }
}