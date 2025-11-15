package com.teckiz.journalindex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Spring configuration for the application
 */
@Configuration
@ComponentScan(basePackages = "com.teckiz.journalindex")
@EnableJpaRepositories(basePackages = "com.teckiz.journalindex.repository")
@EnableTransactionManagement
public class ApplicationConfig {

    @Value("${DB_URL:jdbc:mysql://localhost:3306/journal_index}")
    private String dataSourceUrl;

    @Value("${DB_USERNAME:root}")
    private String dataSourceUsername;

    @Value("${DB_PASSWORD:password}")
    private String dataSourcePassword;

    @Value("${MYSQL_HOST:localhost}")
    private String mysqlHost;

    @Value("${MYSQL_PORT:3306}")
    private String mysqlPort;

    @Value("${MYSQL_DATABASE:journal_index}")
    private String mysqlDatabase;

    @Value("${MYSQL_SSL_MODE:REQUIRED}")
    private String mysqlSslMode;

    @Value("${MYSQL_CONNECTION_TIMEOUT:30000}")
    private String mysqlConnectionTimeout;

    @Value("${MYSQL_SOCKET_TIMEOUT:30000}")
    private String mysqlSocketTimeout;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String dataSourceDriverClassName;

    @Bean
    public DataSource dataSource() {
        // Log connection details for debugging (without password)
        System.out.println("=== Database Connection Debug ===");
        System.out.println("DB_URL: " + dataSourceUrl);
        System.out.println("DB_USERNAME: " + dataSourceUsername);
        System.out.println("DB_DRIVER: " + dataSourceDriverClassName);
        System.out.println("MYSQL_HOST: " + mysqlHost);
        System.out.println("MYSQL_PORT: " + mysqlPort);
        System.out.println("MYSQL_DATABASE: " + mysqlDatabase);
        System.out.println("MYSQL_SSL_MODE: " + mysqlSslMode);
        System.out.println("================================");
        
        // Using HikariCP (lightweight and fast)
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        
        // Use environment-specific URL or construct from components
        String finalUrl = dataSourceUrl;
        boolean hasJdbcPrefix = finalUrl != null && finalUrl.startsWith("jdbc:");
        
        // Extract host - use MYSQL_HOST if set, otherwise extract from DB_URL
        String hostToUse = mysqlHost;
        if (hostToUse == null || hostToUse.isBlank()) {
            if (finalUrl != null && !finalUrl.isBlank()) {
                if (hasJdbcPrefix) {
                    // Extract host from JDBC URL: jdbc:mysql://HOST:PORT/DATABASE
                    try {
                        String urlWithoutPrefix = finalUrl.substring("jdbc:mysql://".length());
                        int colonIndex = urlWithoutPrefix.indexOf(':');
                        int slashIndex = urlWithoutPrefix.indexOf('/');
                        if (colonIndex > 0 && slashIndex > colonIndex) {
                            hostToUse = urlWithoutPrefix.substring(0, colonIndex);
                        }
                    } catch (Exception e) {
                        // If parsing fails, fall back to using DB_URL as host
                        hostToUse = finalUrl;
                    }
                } else {
                    // DB_URL is just the host name
                    hostToUse = finalUrl;
                }
            }
        }
        
        // If we still don't have a valid URL with jdbc: prefix, construct it
        if (!hasJdbcPrefix || finalUrl.contains("${MYSQL_HOST}") || finalUrl.contains("localhost") || hostToUse != null) {
            // Construct URL from components for VPC deployment
            finalUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=%s&serverTimezone=UTC&connectTimeout=%s&socketTimeout=%s",
                    hostToUse != null ? hostToUse : mysqlHost, mysqlPort, mysqlDatabase, mysqlSslMode, mysqlConnectionTimeout, mysqlSocketTimeout);
        }
        
        config.setJdbcUrl(finalUrl);
        config.setUsername(dataSourceUsername);
        config.setPassword(dataSourcePassword);
        config.setDriverClassName(dataSourceDriverClassName);
        
        // VPC-specific connection pool settings (HikariCP - optimized for Lambda cold start)
        config.setMinimumIdle(0);  // Don't create connections during initialization
        config.setMaximumPoolSize(5);  // Reduced from 10 for Lambda
        config.setConnectionTimeout(5000);  // 5 seconds (reduced to prevent timeout)
        config.setIdleTimeout(300000);  // 5 minutes
        config.setMaxLifetime(600000);  // 10 minutes (shorter for Lambda)
        config.setConnectionTestQuery("SELECT 1");
        config.setInitializationFailTimeout(-1);  // Don't fail if pool can't initialize immediately
        config.setRegisterMbeans(false);  // Disable JMX to reduce overhead
        
        return new com.zaxxer.hikari.HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.teckiz.journalindex.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");  // Auto-create tables if missing
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");
        properties.setProperty("hibernate.use_sql_comments", "false");
        properties.setProperty("hibernate.jdbc.batch_size", "25");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        // Lambda-specific optimizations for faster cold start
        properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");  // Skip metadata lookup
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");  // Avoid LOB metadata
        em.setJpaProperties(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}
