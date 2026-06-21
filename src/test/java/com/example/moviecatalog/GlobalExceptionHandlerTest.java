package com.example.moviecatalog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InterruptedIOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleException_returns400() {
        ResponseEntity<String> response = handler.handleException(new RuntimeException("something went wrong"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleException_bodyContainsMessage() {
        ResponseEntity<String> response = handler.handleException(new RuntimeException("something went wrong"));
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("something went wrong"));
    }

    @Test
    void handleIIOException_returns400(){
        ResponseEntity<String> response = handler.handleIIOException(new InterruptedIOException("io error"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleIIOException_bodyContainsMessage() {
        ResponseEntity<String> response = handler.handleIIOException(new InterruptedIOException("io error"));
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("io error"));
    }

    @Test
    void handleIIOException_nullMessage_doesNotThrow() {
        ResponseEntity<String> response = handler.handleIIOException(new InterruptedIOException());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
