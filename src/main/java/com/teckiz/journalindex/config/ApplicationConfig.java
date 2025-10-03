package com.teckiz.journalindex.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring configuration for the application
 */
@Configuration
@ComponentScan(basePackages = "com.teckiz.journalindex")
@EnableJpaRepositories(basePackages = "com.teckiz.journalindex.repository")
@EnableTransactionManagement
public class ApplicationConfig {
    // Configuration class
}
