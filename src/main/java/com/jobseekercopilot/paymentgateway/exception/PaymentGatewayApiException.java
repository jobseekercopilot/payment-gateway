package com.jobseekercopilot.paymentgateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class PaymentGatewayApiException extends RuntimeException {
    private final HttpStatusCode status;
    private final String code;

    public PaymentGatewayApiException(HttpStatusCode status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
