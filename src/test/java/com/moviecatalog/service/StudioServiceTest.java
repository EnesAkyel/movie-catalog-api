package com.moviecatalog.service;

import com.moviecatalog.model.Studio;
import com.moviecatalog.repository.StudioRepository;
import com.moviecatalog.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudioService Tests")
class StudioServiceTest {

    @Mock
    private StudioRepository studioRepository;

    private StudioService studioService;

    private final List<Studio> allStudios = List.of(
            new Studio(1, "Paramount"),
            new Studio(2, "Warner Bros"),
            new Studio(3, "Universal"),
            new Studio(4, "20th Century"),
            new Studio(5, "Miramax")
    );

    @BeforeEach
    void setUp() {
        lenient().when(studioRepository.findAll()).thenReturn(allStudios);
        lenient().when(studioRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(allStudios, PageRequest.of(0, 10), 5));
        studioService = new StudioService(studioRepository);
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
        when(studioRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(999, 10), 5));

        PageResponse<Studio> result = studioService.getStudios(999, 10);
        assertEquals(0, result.getContent().size());
        assertEquals(5, result.getTotalElements());
    }

    @Test
    void getStudios_sizeZero_returnsEmptyContentWithTotal() {
        when(studioRepository.count()).thenReturn(5L);

        PageResponse<Studio> result = studioService.getStudios(0, 0);
        assertEquals(0, result.getContent().size());
        assertEquals(5, result.getTotalElements());
    }

    @Test
    void add_newStudio_returnsPresent() {
        Studio studio = new Studio(100, "New Studio");
        when(studioRepository.existsById(100)).thenReturn(false);
        when(studioRepository.save(studio)).thenReturn(studio);

        Optional<Studio> result = studioService.add(studio);
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getSID());
    }

    @Test
    void add_duplicate_returnsEmpty() {
        when(studioRepository.existsById(100)).thenReturn(true);
        assertFalse(studioService.add(new Studio(100, "Dup Studio")).isPresent());
    }

    @Test
    void add_newStudio_savesViaRepository() {
        Studio studio = new Studio(100, "Added Studio");
        when(studioRepository.existsById(100)).thenReturn(false);
        when(studioRepository.save(studio)).thenReturn(studio);

        studioService.add(studio);
        verify(studioRepository).save(studio);
    }

    @Test
    void update_existingStudio_updatesName() {
        Studio existing = new Studio(100, "Original");
        when(studioRepository.findById(100)).thenReturn(Optional.of(existing));
        when(studioRepository.save(existing)).thenReturn(existing);

        Optional<Studio> result = studioService.update(100, new Studio(100, "Updated"));
        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().getName());
    }

    @Test
    void update_unknownSid_returnsEmpty() {
        when(studioRepository.findById(100)).thenReturn(Optional.empty());
        assertFalse(studioService.update(100, new Studio(100, "Ghost")).isPresent());
    }

    @Test
    void delete_existingStudio_returnsDeletedStudio() {
        Studio studio = new Studio(100, "To Delete");
        when(studioRepository.findById(100)).thenReturn(Optional.of(studio));

        Optional<Studio> result = studioService.delete(100);
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getSID());
    }

    @Test
    void delete_existingStudio_deletesViaRepository() {
        Studio studio = new Studio(100, "Remove Me");
        when(studioRepository.findById(100)).thenReturn(Optional.of(studio));

        studioService.delete(100);
        verify(studioRepository).delete(studio);
    }

    @Test
    void delete_unknownSid_returnsEmpty() {
        when(studioRepository.findById(999)).thenReturn(Optional.empty());
        assertFalse(studioService.delete(999).isPresent());
    }
}
