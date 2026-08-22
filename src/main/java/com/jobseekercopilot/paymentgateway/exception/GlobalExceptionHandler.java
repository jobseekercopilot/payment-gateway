package com.jobseekercopilot.paymentgateway.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<PaymentGatewayErrorResponse> validation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(error("VALIDATION_FAILED", "Validation failed"));
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<PaymentGatewayErrorResponse> badRequest(BadRequestException exception) {
        return ResponseEntity.badRequest()
                .body(error("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(PaymentGatewayApiException.class)
    ResponseEntity<PaymentGatewayErrorResponse> api(PaymentGatewayApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<PaymentGatewayErrorResponse> downstreamResponse(
            RestClientResponseException exception) {
        String code = "PAYMENT_DOWNSTREAM_REJECTED";
        String message = "Payment request was rejected";
        try {
            JsonNode body = objectMapper.readTree(exception.getResponseBodyAsByteArray());
            if (body.hasNonNull("code")) code = body.path("code").asText(code);
            else if (body.hasNonNull("error")) code = body.path("error").asText(code);
            if (body.hasNonNull("message")) message = body.path("message").asText(message);
        } catch (IOException ignored) {
            // Return a stable safe error when a downstream response is not JSON.
        }
        return ResponseEntity.status(exception.getStatusCode())
                .body(error(code, message));
    }

    @ExceptionHandler(RestClientException.class)
    ResponseEntity<PaymentGatewayErrorResponse> downstreamFailure(RestClientException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(error("DOWNSTREAM_FAILURE", "Payment downstream failed"));
    }

    private PaymentGatewayErrorResponse error(String code, String message) {
        return new PaymentGatewayErrorResponse(code, code, message);
    }
}
