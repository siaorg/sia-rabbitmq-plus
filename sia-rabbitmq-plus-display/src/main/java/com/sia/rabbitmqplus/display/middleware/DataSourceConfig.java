package com.sia.rabbitmqplus.display.middleware;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Created by xinliang on 2017/9/10.
 */
@Configuration
// @MapperScan(basePackages = "com.creditease.skytrain.display.mapper", sqlSessionTemplateRef = "sqlSessionTemplate")
public class DataSourceConfig {

    @Bean(name = "skytrainDataSource")
    @Qualifier("skytrainDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.skytrain")
    public DataSource skytrainDataSource() {

        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("skytrainDataSource") DataSource dataSource)
            throws Exception {

        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "transactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("skytrainDataSource") DataSource dataSource) {

        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory)
            throws Exception {

        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
