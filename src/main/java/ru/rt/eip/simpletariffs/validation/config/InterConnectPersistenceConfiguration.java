package ru.rt.eip.simpletariffs.validation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        basePackages = "ru.rt.eip.simpletariffs.validation.repository.itc",
        entityManagerFactoryRef = "itcEntityManagerFactory",
        transactionManagerRef = "itcTransactionManager")
public class InterConnectPersistenceConfiguration {

    private final Environment env;

    @Bean(destroyMethod = "")
    @ConfigurationProperties(prefix = "spring.itc-datasource")
    public DataSource itcDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean itcEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(itcDataSource());
        em.setPackagesToScan("ru.rt.eip.simpletariffs.validation.repository.itc");
        em.setPersistenceUnitName("itcEntityManager");

        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager itcTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(itcEntityManagerFactory().getObject());
        return transactionManager;
    }

}
