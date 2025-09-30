package com.teckiz.journalindex.config;

import org.apache.camel.component.sql.SqlComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Database configuration for MySQL connection
 */
@Configuration
public class DatabaseConfig {
    
    @Autowired
    private DataSource dataSource;
    
    @Bean(name = "sqlComponent")
    public SqlComponent sqlComponent() {
        SqlComponent sqlComponent = new SqlComponent();
        sqlComponent.setDataSource(dataSource);
        return sqlComponent;
    }
}
