package com.example.document.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class DocumentUploadFailedException extends HttpClientErrorException {


    public DocumentUploadFailedException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
    }
}
