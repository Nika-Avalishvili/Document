package com.example.document.exceptionHandler;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BenefitNotFoundException extends WebClientResponseException {

    public BenefitNotFoundException(WebClientResponseException.NotFound exception) {
        super(exception.getMessage(),
                exception.getRawStatusCode(),
                exception.getStatusText(),
                exception.getHeaders(),
                exception.getResponseBodyAsByteArray(),
                StandardCharsets.UTF_8);
    }

}
