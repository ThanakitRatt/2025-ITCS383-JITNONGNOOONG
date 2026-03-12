package com.itcs383.restaurant.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration for accessing mhar_auth database for user authentication.
 * Also explicitly defines the primary datasource to prevent Spring Boot
 * autoconfiguration from being displaced by the secondary authDataSource bean.
 */
@Configuration
public class AuthDataSourceConfig {

    /**
     * Primary datasource properties bound from spring.datasource in application.yml
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Primary datasource for mhar_restaurants - used by JPA/Hibernate
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("dataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "authDataSource")
    public DataSource authDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mhar_auth");
        config.setUsername(System.getenv().getOrDefault("DB_USERNAME", "mhar_user"));
        config.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "mhar_password"));
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }

    @Bean(name = "authJdbcTemplate")
    public JdbcTemplate authJdbcTemplate(@Qualifier("authDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
