package com.moviecatalog.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioModelTest {

    private Studio studio(int sid) {
        return new Studio(sid, "Test Studio");
    }

    @Test
    void hashCode_sameSid_returnsSameValue() {
        Studio a = studio(10);
        Studio b = studio(10);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_differentSid_returnsDifferentValue() {
        assertNotEquals(studio(10).hashCode(), studio(20).hashCode());
    }

    @Test
    void equals_sameSid_returnsTrue() {
        Studio a = studio(10);
        Studio b = studio(10);
        assertEquals(a, b);
    }

    @Test
    void equals_differentSid_returnsFalse() {
        assertNotEquals(studio(10), studio(20));
    }

    @Test
    void equals_null_returnsFalse() {
        assertNotEquals(studio(10), null);
    }

    @Test
    void equals_differentClass_returnsFalse() {
        assertNotEquals(studio(10), new Object());
    }

    @Test
    void compareTo_lowerSidComesFirst() {
        assertTrue(studio(10).compareTo(studio(20)) < 0);
    }

    @Test
    void compareTo_higherSidComesLast() {
        assertTrue(studio(20).compareTo(studio(10)) > 0);
    }

    @Test
    void compareTo_sameSid_returnsZero() {
        Studio a = studio(10);
        Studio b = studio(10);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void toString_containsSidAndName() {
        Studio s = studio(10);
        String result = s.toString();
        assertTrue(result.contains("10"));
        assertTrue(result.contains("Test Studio"));
    }

    @Test
    void compareTo_sortsList_inAscendingOrder() {
        List<Studio> studios = Arrays.asList(studio(30), studio(10), studio(20));
        Collections.sort(studios);
        assertEquals(10, studios.get(0).getSID());
        assertEquals(20, studios.get(1).getSID());
        assertEquals(30, studios.get(2).getSID());
    }
}
