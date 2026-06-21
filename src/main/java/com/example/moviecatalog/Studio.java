package com.example.moviecatalog;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;

public class Studio implements Comparable<Studio> {
    @NotNull(message = "SID must be present")
    @Digits(integer=3,fraction=0, message = "SID must be an integer with up to 3 digits")
    @Range(min=1,max=100, message = "SID must be between 1 and 100")
    @Positive(message = "Studio ID must be bigger than 0")
    private int SID;

    @NotEmpty(message = "Name must have length greater than 0")
    @NotNull(message = "Name must be present")
    private String name;

    public Studio() {}

    public Studio(int SID, String name) {
        this.SID = SID;
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(SID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Studio other = (Studio) obj;
        return this.SID == other.SID;
    }

    @Override
    public String toString() {
        return "Studio{" + "SID=" + SID + ", name=" + name + '}';
    }

    public int getSID() {
        return SID;
    }

    public void setSID(int SID) {
        this.SID = SID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Studio other) {
        return Integer.compare(this.SID, other.SID);
    }
}
