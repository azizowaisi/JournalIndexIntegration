package com.teckiz.journalindex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration for enabling repositories and transaction management
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.teckiz.journalindex.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration is handled by Spring Boot auto-configuration
    // This class just enables the necessary annotations
}
