package com.modsen.apigateway.exception;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiErrorMessage {

    private HttpStatusCode status;

    private String errorMessage;

    private LocalDateTime timestamp;
}
