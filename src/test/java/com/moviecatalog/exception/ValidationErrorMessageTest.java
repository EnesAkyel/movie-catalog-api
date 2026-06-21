package com.moviecatalog.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationErrorMessageTest {

    @Test
    void defaultMessage_isSpringValidationError() {
        assertEquals("Spring Validation Error", new ValidationErrorMessage().getMessage());
    }

    @Test
    void setMessage_updatesMessage() {
        ValidationErrorMessage v = new ValidationErrorMessage();
        v.setMessage("Custom Error");
        assertEquals("Custom Error", v.getMessage());
    }

    @Test
    void errors_emptyByDefault() {
        assertTrue(new ValidationErrorMessage().getErrors().isEmpty());
    }

    @Test
    void addFieldErrorMessage_addsToList() {
        ValidationErrorMessage v = new ValidationErrorMessage();
        FieldErrorMessage f = new FieldErrorMessage();
        f.setField("name");
        f.setMessage("Name is required");

        v.addFieldErrorMessage(f);

        assertEquals(1, v.getErrors().size());
        assertEquals("name", v.getErrors().getFirst().getField());
        assertEquals("Name is required", v.getErrors().getFirst().getMessage());
    }

    @Test
    void addFieldErrorMessage_multipleErrors_allPresent() {
        ValidationErrorMessage v = new ValidationErrorMessage();
        FieldErrorMessage f1 = new FieldErrorMessage();
        f1.setField("name");
        f1.setMessage("Name is required");
        FieldErrorMessage f2 = new FieldErrorMessage();
        f2.setField("genre");
        f2.setMessage("Genre is invalid");

        v.addFieldErrorMessage(f1);
        v.addFieldErrorMessage(f2);

        assertEquals(2, v.getErrors().size());
    }
}
