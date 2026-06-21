package com.moviecatalog.service;

import com.moviecatalog.model.Studio;
import com.moviecatalog.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioServiceTest {

    private StudioService studioService;

    @BeforeEach
    void setUp() {
        studioService = new StudioService();
    }

    @Test
    void getAll_returns5PreloadedStudios() {
        assertEquals(5, studioService.getAll().size());
    }

    @Test
    void getStudios_returnsPagedResults() {
        PageResponse<Studio> result = studioService.getStudios(0, 10);
        assertEquals(5, result.getTotalElements());
        assertEquals(5, result.getContent().size());
    }

    @Test
    void getStudios_pageOutOfRange_returnsEmptyContent() {
        PageResponse<Studio> result = studioService.getStudios(999, 10);
        assertEquals(0, result.getContent().size());
        assertEquals(5, result.getTotalElements());
    }

    @Test
    void add_newStudio_returnsPresent() {
        Optional<Studio> result = studioService.add(new Studio(100, "New Studio"));
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getSID());
    }

    @Test
    void add_duplicate_returnsEmpty() {
        studioService.add(new Studio(100, "Dup Studio"));
        Optional<Studio> result = studioService.add(new Studio(100, "Dup Studio"));
        assertFalse(result.isPresent());
    }

    @Test
    void add_newStudio_appearsInGetAll() {
        studioService.add(new Studio(100, "Added Studio"));
        assertTrue(studioService.getAll().stream().anyMatch(s -> s.getSID() == 100));
    }

    @Test
    void update_existingStudio_updatesName() {
        studioService.add(new Studio(100, "Original"));
        Optional<Studio> result = studioService.update(100, new Studio(100, "Updated"));
        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().getName());
    }

    @Test
    void update_unknownSid_returnsEmpty() {
        Optional<Studio> result = studioService.update(100, new Studio(100, "Ghost"));
        assertFalse(result.isPresent());
    }

    @Test
    void delete_existingStudio_returnsDeletedStudio() {
        studioService.add(new Studio(100, "To Delete"));
        Optional<Studio> result = studioService.delete(100);
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getSID());
    }

    @Test
    void delete_existingStudio_removesFromList() {
        studioService.add(new Studio(100, "Remove Me"));
        studioService.delete(100);
        assertFalse(studioService.getAll().stream().anyMatch(s -> s.getSID() == 100));
    }

    @Test
    void delete_unknownSid_returnsEmpty() {
        Optional<Studio> result = studioService.delete(999);
        assertFalse(result.isPresent());
    }
}