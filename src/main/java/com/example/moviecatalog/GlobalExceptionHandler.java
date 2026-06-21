package com.example.moviecatalog;

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
            String result = "Exception: " + ex.getMessage() + ":" + ex;
            logger.warn("{}", ex.getClass());
            logger.warn("{}", result);
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(InterruptedIOException.class)
        public ResponseEntity<String> handleIIOException(InterruptedIOException ex) {
            String result = "InterruptedIOException: " + ex.getMessage();
            logger.warn("{}", ex.getClass());
            logger.warn("{}", result);
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ValidationErrorMessage> handleValidationException(MethodArgumentNotValidException ex) {
            logger.error("We have an exception: MethodArgumentNotValidException");
            ValidationErrorMessage validationError = new ValidationErrorMessage();
            BindingResult bindingResult = ex.getBindingResult();

            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                logger.error("{}", fieldError.getCode());
                logger.error("{}", fieldError.getField());
                logger.error("{}", fieldError);
                logger.error("{}", fieldError.getDefaultMessage());

                FieldErrorMessage fieldErrorMessage = new FieldErrorMessage();
                fieldErrorMessage.setField(fieldError.getField());
                fieldErrorMessage.setMessage(fieldError.getDefaultMessage());
                validationError.addFieldErrorMessage(fieldErrorMessage);
            }

            return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
        }
}
