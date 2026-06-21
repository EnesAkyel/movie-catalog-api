package com.moviecatalog.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageResponseTest {

    @Test
    void constructor_setsAllFields() {
        List<String> content = List.of("a", "b");
        PageResponse<String> page = new PageResponse<>(content, 1, 5, 20);

        assertEquals(content, page.getContent());
        assertEquals(1, page.getPage());
        assertEquals(5, page.getSize());
        assertEquals(20, page.getTotalElements());
    }

    @Test
    void totalPages_exactDivision() {
        PageResponse<String> page = new PageResponse<>(List.of(), 0, 5, 10);
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void totalPages_withRemainder() {
        PageResponse<String> page = new PageResponse<>(List.of(), 0, 3, 10);
        assertEquals(4, page.getTotalPages());
    }

    @Test
    void totalPages_zeroElements_returnsZero() {
        PageResponse<String> page = new PageResponse<>(List.of(), 0, 10, 0);
        assertEquals(0, page.getTotalPages());
    }

    @Test
    void totalPages_sizeZero_returnsZero() {
        PageResponse<String> page = new PageResponse<>(List.of(), 0, 0, 10);
        assertEquals(0, page.getTotalPages());
    }
}
