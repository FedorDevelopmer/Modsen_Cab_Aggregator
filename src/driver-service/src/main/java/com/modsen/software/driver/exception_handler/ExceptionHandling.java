package com.modsen.software.driver.exception_handler;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

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
