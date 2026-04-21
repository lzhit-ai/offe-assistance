package com.example.getoffer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class ApplicationPropertiesTest {

    @Test
    void productionHibernateDialectUsesSupportedMySqlDialect() throws IOException {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        }

        assertEquals(
                "org.hibernate.dialect.MySQLDialect",
                properties.getProperty("spring.jpa.properties.hibernate.dialect")
        );
    }

    @Test
    void demoDataSeedingIsDisabledByDefaultInProductionProperties() throws IOException {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        }

        assertEquals(
                "${APP_SEED_DEMO_DATA:false}",
                properties.getProperty("app.seed-demo-data")
        );
    }
}
