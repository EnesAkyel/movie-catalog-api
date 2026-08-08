package com.moviecatalog.config;

import com.moviecatalog.auth.JwtUtil;
import com.moviecatalog.service.MovieService;
import com.moviecatalog.service.StudioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private StudioService studioService;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401")
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/movies"))
                .andExpect(status().isUnauthorized());
    }
}
