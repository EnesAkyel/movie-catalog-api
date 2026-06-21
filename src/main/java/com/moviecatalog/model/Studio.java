package com.moviecatalog.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;

public class Studio implements Comparable<Studio> {
    @NotNull(message = "Studio ID must be present")
    @Digits(integer=3,fraction=0, message = "Studio ID must be an integer with up to 3 digits")
    @Range(min=1,max=100, message = "Studio ID must be between 1 and 100")
    @Positive(message = "Studio ID must be bigger than 0")
    private int sid;

    @NotEmpty(message = "Name must have length greater than 0")
    @NotNull(message = "Name must be present")
    private String name;

    public Studio() {}

    public Studio(int sid, String name) {
        this.sid = sid;
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(sid);
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
        return this.sid == other.sid;
    }

    @Override
    public String toString() {
        return "Studio{" + "SID=" + sid + ", name=" + name + '}';
    }

    public int getSID() {
        return sid;
    }

    public void setSID(int sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Studio other) {
        return Integer.compare(this.sid, other.sid);
    }
}
