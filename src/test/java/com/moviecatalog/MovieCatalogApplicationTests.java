package com.moviecatalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestContainersConfig.class)
@DisplayName("Application Context Tests")
class MovieCatalogApplicationTests {

    @Test
    void contextLoads() {
    }
}
