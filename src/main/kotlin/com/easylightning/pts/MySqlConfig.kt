package com.easylightning.pts

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.persistence.EntityManager
import javax.sql.DataSource


/*
 * Created by liyicheng at 2020-11-02 22:50
 */


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactoryPrimary",
        transactionManagerRef = "transactionManagerPrimary",
        basePackages = ["com.easylightning.pts.dao.mysql"]) //设置Repository所在位置
class PrimaryConfig {
    @Autowired
    @Qualifier("mysqlDataSource")
    private val primaryDataSource: DataSource? = null
    @Primary
    @Bean(name = ["entityManagerPrimary"])
    fun entityManager(builder: EntityManagerFactoryBuilder): EntityManager {
        return entityManagerFactoryPrimary(builder).getObject()!!.createEntityManager()
    }

    @Primary
    @Bean(name = ["entityManagerFactoryPrimary"])
    fun entityManagerFactoryPrimary(builder: EntityManagerFactoryBuilder): LocalContainerEntityManagerFactoryBean {
        return builder
                .dataSource(primaryDataSource)
                .properties(jpaProperties.properties)
                .packages("com.easylightning.pts.entity") //设置实体类所在位置
                .persistenceUnit("primaryPersistenceUnit")
                .build()
    }

    @Autowired
    private lateinit var jpaProperties: JpaProperties
//    private fun getVendorProperties(dataSource: DataSource?): Map<String, String> {
//        jpaProperties.properties
//        return jpaProperties.getHibernateProperties(dataSource)
//    }

    @Primary
    @Bean(name = ["transactionManagerPrimary"])
    fun transactionManagerPrimary(builder: EntityManagerFactoryBuilder): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactoryPrimary(builder).getObject()!!)
    }
}