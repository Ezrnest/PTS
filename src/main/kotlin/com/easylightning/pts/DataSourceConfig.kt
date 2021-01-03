package com.easylightning.pts

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource


@Configuration
class DataSourceConfig {
    @Bean(name = ["mysqlDataSource"])
    @Qualifier("mysqlDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    fun primaryDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean(name = ["iotdbDataSource"])
    @Qualifier("iotdbDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    fun secondaryDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean(name = ["mysqlTemplate"])
    fun primaryJdbcTemplate(
            @Qualifier("mysqlDataSource") dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }

    @Bean(name = ["iotdbTemplate"])
    fun secondaryJdbcTemplate(
            @Qualifier("iotdbDataSource") dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }
}