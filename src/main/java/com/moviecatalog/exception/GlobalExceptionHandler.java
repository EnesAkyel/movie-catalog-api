package com.moviecatalog.exception;

import java.io.InterruptedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleException(Exception ex) {
            logger.warn("Unhandled exception", ex);
            return new ResponseEntity<>("An unexpected error occurred", HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(InterruptedIOException.class)
        public ResponseEntity<String> handleIIOException(InterruptedIOException ex) {
            logger.warn("Unhandled InterruptedIOException", ex);
            return new ResponseEntity<>("An unexpected error occurred", HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ValidationErrorMessage> handleValidationException(MethodArgumentNotValidException ex) {
            ValidationErrorMessage validationError = new ValidationErrorMessage();
            BindingResult bindingResult = ex.getBindingResult();

            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                logger.warn("Validation error on '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
                FieldErrorMessage fieldErrorMessage = new FieldErrorMessage();
                fieldErrorMessage.setField(fieldError.getField());
                fieldErrorMessage.setMessage(fieldError.getDefaultMessage());
                validationError.addFieldErrorMessage(fieldErrorMessage);
            }

            return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
        }
}
