package com.moviecatalog.config;

import com.moviecatalog.repository.MovieRepository;
import com.moviecatalog.repository.StudioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer Tests")
class DataInitializerTest {

    @Mock
    private StudioRepository studioRepository;

    @Mock
    private MovieRepository movieRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(studioRepository, movieRepository);
    }

    @Test
    void run_whenDataAlreadyExists_skipsSeeding() {
        when(studioRepository.count()).thenReturn(5L);

        dataInitializer.run();

        verify(studioRepository, never()).saveAll(any());
        verify(movieRepository, never()).saveAll(any());
    }

    @Test
    void run_whenEmpty_seedsStudiosAndMovies() {
        when(studioRepository.count()).thenReturn(0L);

        dataInitializer.run();

        verify(studioRepository).saveAll(any());
        verify(movieRepository).saveAll(any());
    }
}
