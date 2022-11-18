package com.example.document.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class EmployeeNotFoundException extends HttpClientErrorException {
    public EmployeeNotFoundException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
    }
}
