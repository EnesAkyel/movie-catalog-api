package com.moviecatalog.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.InterruptedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleException_returns400() {
        ResponseEntity<String> response = handler.handleException(new RuntimeException("something went wrong"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleException_bodyContainsGenericMessage() {
        ResponseEntity<String> response = handler.handleException(new RuntimeException("something went wrong"));
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("An unexpected error occurred"));
    }

    @Test
    void handleIIOException_returns400(){
        ResponseEntity<String> response = handler.handleIIOException(new InterruptedIOException("io error"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleIIOException_bodyContainsGenericMessage() {
        ResponseEntity<String> response = handler.handleIIOException(new InterruptedIOException("io error"));
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("An unexpected error occurred"));
    }

    @Test
    void handleIIOException_nullMessage_doesNotThrow() {
        ResponseEntity<String> response = handler.handleIIOException(new InterruptedIOException());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleValidationException_returns400WithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("movie", "mid", "Movie ID must be a 4 digit number");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ValidationErrorMessage> response = handler.handleValidationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mid", response.getBody().getErrors().getFirst().getField());
    }
}
