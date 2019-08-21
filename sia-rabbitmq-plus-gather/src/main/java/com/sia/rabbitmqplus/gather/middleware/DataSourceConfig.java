package com.sia.rabbitmqplus.gather.middleware;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author xinliang on 2017/9/8.
 */
@Configuration
public class DataSourceConfig {

    @Bean(name = "siaDataSource")
    @Qualifier("siaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.sia")
    public DataSource siaDataSource() {

        return DataSourceBuilder.create().build();
    }

    @Bean(name = "siaJdbcTemplate")
    public JdbcTemplate siaJdbcTemplate(@Qualifier("siaDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
