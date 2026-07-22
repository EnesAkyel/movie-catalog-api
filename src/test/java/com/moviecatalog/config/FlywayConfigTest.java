package com.moviecatalog.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FlywayConfig Tests")
class FlywayConfigTest {

    private final FlywayConfig config = new FlywayConfig();

    @Test
    void flyway_callsMigrate() {
        DataSource dataSource = mock(DataSource.class);
        FluentConfiguration fluentConfig = mock(FluentConfiguration.class);
        Flyway flyway = mock(Flyway.class);

        try (MockedStatic<Flyway> mockedFlyway = mockStatic(Flyway.class)) {
            mockedFlyway.when(Flyway::configure).thenReturn(fluentConfig);
            when(fluentConfig.dataSource(dataSource)).thenReturn(fluentConfig);
            when(fluentConfig.locations(any(String[].class))).thenReturn(fluentConfig);
            when(fluentConfig.load()).thenReturn(flyway);

            config.flyway(dataSource);

            verify(flyway).migrate();
        }
    }

    @Test
    void flyway_returnsFlywayInstance() {
        DataSource dataSource = mock(DataSource.class);
        FluentConfiguration fluentConfig = mock(FluentConfiguration.class);
        Flyway flyway = mock(Flyway.class);

        try (MockedStatic<Flyway> mockedFlyway = mockStatic(Flyway.class)) {
            mockedFlyway.when(Flyway::configure).thenReturn(fluentConfig);
            when(fluentConfig.dataSource(dataSource)).thenReturn(fluentConfig);
            when(fluentConfig.locations(any(String[].class))).thenReturn(fluentConfig);
            when(fluentConfig.load()).thenReturn(flyway);

            Flyway result = config.flyway(dataSource);

            assertNotNull(result);
        }
    }

    @Test
    void flywayDependencyConfigurer_addsDependencyWhenEntityManagerFactoryPresent() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("entityManagerFactory", new GenericBeanDefinition());

        BeanFactoryPostProcessor postProcessor = FlywayConfig.flywayDependencyConfigurer();
        postProcessor.postProcessBeanFactory(beanFactory);

        BeanDefinition bd = beanFactory.getBeanDefinition("entityManagerFactory");
        assertNotNull(bd.getDependsOn());
        assertTrue(java.util.Arrays.asList(bd.getDependsOn()).contains("flyway"));
    }

    @Test
    void flywayDependencyConfigurer_doesNothingWhenEntityManagerFactoryAbsent() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        BeanFactoryPostProcessor postProcessor = FlywayConfig.flywayDependencyConfigurer();
        assertDoesNotThrow(() -> postProcessor.postProcessBeanFactory(beanFactory));
    }
}