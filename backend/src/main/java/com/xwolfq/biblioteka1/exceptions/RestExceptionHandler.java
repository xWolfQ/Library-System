package com.xwolfq.biblioteka1.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(BookLoanedException.class)
    public ResponseEntity<String> handleBookLoaned(BookLoanedException ex, HttpServletRequest request) {
        Date now = Date.from(Instant.now());
        String body = ex.getMessage() + " | Czas: " + now.toString();

        log.warn("BookLoanedException - path={}, status={}, message={}", request.getRequestURI(),
                HttpStatus.CONFLICT.value(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ReaderLoanedException.class)
    public ResponseEntity<String> handleReaderLoaned(ReaderLoanedException ex, HttpServletRequest request) {
        Date now = Date.from(Instant.now());
        String body = ex.getMessage() + " | Czas: " + now.toString();

        log.warn("ReaderLoanedException - path={}, status={}, message={}", request.getRequestURI(),
                HttpStatus.CONFLICT.value(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

}
