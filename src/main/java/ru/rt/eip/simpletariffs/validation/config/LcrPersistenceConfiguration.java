package ru.rt.eip.simpletariffs.validation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        basePackages = "ru.rt.eip.simpletariffs.validation.repository.lcr",
        entityManagerFactoryRef = "lcrEntityManagerFactory",
        transactionManagerRef = "lcrTransactionManager")
public class LcrPersistenceConfiguration {

    private final Environment env;

    @Bean(destroyMethod = "")
    @Primary
    @ConfigurationProperties(prefix = "spring.lcr-datasource")
    public DataSource lcrDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean lcrEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(lcrDataSource());
        em.setPackagesToScan("ru.rt.eip.simpletariffs.validation.repository.lcr");
        em.setPersistenceUnitName("lcrEntityManager");

        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager lcrTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(lcrEntityManagerFactory().getObject());
        return transactionManager;
    }
}
