package com.example.document.exceptionHandler;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.Charset;

public class BenefitNotFoundException extends WebClientResponseException {
    public BenefitNotFoundException(String message, int statusCode, String statusText, HttpHeaders headers, byte[] responseBody, Charset charset) {
        super(message, statusCode, statusText, headers, responseBody, charset);
    }

}
