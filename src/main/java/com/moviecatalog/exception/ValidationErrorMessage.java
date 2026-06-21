package com.moviecatalog.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ValidationErrorMessage implements Serializable {
    private String message = "Spring Validation Error";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private final List<FieldErrorMessage> errors = new ArrayList<>();

    public List<FieldErrorMessage> getErrors() {
        return errors;
    }

    public void addFieldErrorMessage(FieldErrorMessage message) {
        errors.add(message);
    }
}
