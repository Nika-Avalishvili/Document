package com.example.document.exceptionHandler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class DocumentUploadFailedExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {EmployeeNotFoundException.class})
    protected ResponseEntity<Object> handleEmployeeNotFoundConflict(RuntimeException ex, WebRequest request) {

        String errorMessage = "\"Document upload failed!\"\n" + ex.getMessage().split(":")[1].strip();

        return handleExceptionInternal(ex, errorMessage,
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {BenefitNotFoundException.class})
    protected ResponseEntity<Object> handleBenefitNotFoundConflict(RuntimeException ex, WebRequest request) {
        String[] text = ex.getMessage().split("/");

        String errorMessage = "\"Document upload failed!\"\n" + String.format("\"Benefit with benefit_id %s doesn't exist!\"", text[4]);

        return handleExceptionInternal(ex, errorMessage,
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}