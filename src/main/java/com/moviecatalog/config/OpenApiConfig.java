package com.moviecatalog.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Movie Catalog API",
        version = "v1",
        description = "RESTful API for managing a catalog of movies and studios"
    )
)
public class OpenApiConfig {}
