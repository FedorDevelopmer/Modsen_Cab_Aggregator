package com.modsen.software.apigateway.fallback_controller;

import com.modsen.software.apigateway.entity.FallbackMessage;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/driver_service")
    public ResponseEntity<FallbackMessage> driverServiceFallBack(@RequestHeader(name = "X-Original-Url", required = false) String uri, ServerWebExchange exchange) {
        FallbackMessage fallbackMessage = FallbackMessage.builder()
                .message("Driver service temporary unavailable.")
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .uri(uri != null ? uri : "Original URI not detected")
                .method(exchange.getRequest().getMethod().toString())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(fallbackMessage, HttpStatusCode.valueOf(200));
    }

    @RequestMapping("/fallback/passenger_service")
    public ResponseEntity<FallbackMessage> passengerServiceFallBack(@RequestHeader(name = "X-Original-Url", required = false) String uri, ServerWebExchange exchange) {
        FallbackMessage fallbackMessage = FallbackMessage.builder()
                .message("Passenger service temporary unavailable.")
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .uri(uri != null ? uri : "Original URI not detected")
                .method(exchange.getRequest().getMethod().toString())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(fallbackMessage, HttpStatusCode.valueOf(200));
    }

    @RequestMapping("/fallback/ride_service")
    public ResponseEntity<FallbackMessage> rideServiceFallBack(@RequestHeader(name = "X-Original-Url", required = false) String uri, ServerWebExchange exchange) {
        FallbackMessage fallbackMessage = FallbackMessage.builder()
                .message("Ride service temporary unavailable.")
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .uri(uri != null ? uri : "Original URI not detected")
                .method(exchange.getRequest().getMethod().toString())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(fallbackMessage, HttpStatusCode.valueOf(200));
    }

    @RequestMapping("/fallback/rating_service")
    public ResponseEntity<FallbackMessage> ratingServiceFallBack(@RequestHeader(name = "X-Original-Url", required = false) String uri, ServerWebExchange exchange) {
        FallbackMessage fallbackMessage = FallbackMessage.builder()
                .message("Rating service temporary unavailable.")
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .uri(uri != null ? uri : "Original URI not detected")
                .method(exchange.getRequest().getMethod().toString())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(fallbackMessage, HttpStatusCode.valueOf(200));
    }
}
