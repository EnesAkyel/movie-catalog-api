package com.example.moviecatalog;

import java.io.Serializable;
import java.util.ArrayList;

public class ValidationErrorMessage implements Serializable {
    private String message = "Spring Validation Error";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private ArrayList<FieldErrorMessage> errors = new ArrayList();

    public ArrayList<FieldErrorMessage> getErrors() {
        return errors;
    }

    public void addFieldErrorMessage(FieldErrorMessage message) {
        errors.add(message);
    }
}
