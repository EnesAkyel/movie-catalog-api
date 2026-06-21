package com.example.moviecatalog;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovieModelTest {

    private Movie movie(int mid) {
        Movie m = new Movie();
        m.setMID(mid);
        m.setName("Test Movie");
        m.setGenre("Action");
        m.setPrice(9.99);
        m.setRating("PG-13");
        m.setStudio(1);
        return m;
    }

    @Test
    void hashCode_sameMid_returnsSameValue() {
        assertEquals(movie(1000).hashCode(), movie(1000).hashCode());
    }

    @Test
    void hashCode_differentMid_returnsDifferentValue() {
        assertNotEquals(movie(1000).hashCode(), movie(2000).hashCode());
    }

    @Test
    void equals_sameMid_returnsTrue() {
        Movie a = movie(1000);
        Movie b = movie(1000);
        assertEquals(a, b);
    }

    @Test
    void equals_differentMid_returnsFalse() {
        assertNotEquals(movie(1000), movie(2000));
    }

    @Test
    void equals_null_returnsFalse() {
        assertNotEquals(null, movie(1000));
    }

    @Test
    void compareTo_lowerMidComesFirst() {
        assertTrue(movie(1000).compareTo(movie(2000)) < 0);
    }

    @Test
    void compareTo_higherMidComesLast() {
        assertTrue(movie(2000).compareTo(movie(1000)) > 0);
    }

    @Test
    void compareTo_sameMid_returnsZero() {
        assertEquals(0, movie(1000).compareTo(movie(1000)));
    }

    @Test
    void compareTo_sortsList_inAscendingOrder() {
        List<Movie> movies = Arrays.asList(movie(3000), movie(1000), movie(2000));
        Collections.sort(movies);
        assertEquals(1000, movies.get(0).getMID());
        assertEquals(2000, movies.get(1).getMID());
        assertEquals(3000, movies.get(2).getMID());
    }
}
