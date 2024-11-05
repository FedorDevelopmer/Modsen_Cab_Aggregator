package com.modsen.apigateway.exception_handler;

import com.modsen.apigateway.exception.ApiErrorMessage;
import java.net.ConnectException;
import java.time.LocalDateTime;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorMessage onInternalServerError(ConnectException e) {
        return ApiErrorMessage.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorMessage(e.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorMessage onNotFoundResponse(NoResourceFoundException e) {
        return ApiErrorMessage.builder()
                .status(HttpStatus.NOT_FOUND)
                .errorMessage(e.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiErrorMessage onUnavailableServiceError(NotFoundException e) {
        return ApiErrorMessage.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .errorMessage(e.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
