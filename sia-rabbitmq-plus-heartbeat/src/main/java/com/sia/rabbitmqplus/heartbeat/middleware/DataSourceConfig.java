//package com.creditease.skytrain.supervise.middleware;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import javax.sql.DataSource;
//
///**
// * @author xinliang on 2017/9/8.
// */
//@Configuration
//public class DataSourceConfig {
//
//    @Bean(name = "skytrainDataSource")
//    @Qualifier("skytrainDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.skytrain")
//    public DataSource skytrainDataSource() {
//
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "skytrainJdbcTemplate")
//    public JdbcTemplate skytrainJdbcTemplate(@Qualifier("skytrainDataSource") DataSource dataSource) {
//
//        return new JdbcTemplate(dataSource);
//    }
//
//}
