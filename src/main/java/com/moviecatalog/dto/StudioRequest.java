package com.moviecatalog.dto;

import com.moviecatalog.model.Studio;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;

public record StudioRequest(
        @NotNull(message = "Studio ID must be present")
        @Digits(integer = 3, fraction = 0, message = "Studio ID must be an integer with up to 3 digits")
        @Range(min = 1, max = 100, message = "Studio ID must be between 1 and 100")
        @Positive(message = "Studio ID must be bigger than 0")
        int sid,

        @NotEmpty(message = "Name must have length greater than 0")
        @NotNull(message = "Name must be present")
        String name
) {
    public Studio toStudio() {
        return new Studio(this.sid, this.name);
    }
}
