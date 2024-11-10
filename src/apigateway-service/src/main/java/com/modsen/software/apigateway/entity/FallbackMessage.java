package com.modsen.software.apigateway.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FallbackMessage {

    private String message;

    private String method;

    private String uri;

    private HttpStatus status;

    private LocalDateTime timestamp;

}
