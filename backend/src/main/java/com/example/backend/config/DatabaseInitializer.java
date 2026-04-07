package com.example.backend.config;

import com.example.backend.repository.ModuleRepository;
import com.example.backend.repository.HistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Database initializer that runs on application startup.
 * Verifies the database connection is healthy and logs table status.
 * Depends on flyway to ensure migrations run before accessing repositories.
 */
@Component
@DependsOn("flyway")
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Override
    public void run(String... args) {
        verifyDatabaseConnection();
        logTableStatus();
    }

    private void verifyDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            logger.info("========================================");
            logger.info("Database connection established successfully!");
            logger.info("Database URL: {}", metaData.getURL());
            logger.info("Database Product: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
            logger.info("JDBC Driver: {} {}", metaData.getDriverName(), metaData.getDriverVersion());
            logger.info("========================================");
        } catch (Exception e) {
            logger.error("Failed to verify database connection: {}", e.getMessage(), e);
        }
    }

    private void logTableStatus() {
        try {
            long moduleCount = moduleRepository.count();
            long historyCount = historyRepository.count();
            logger.info("Table 'modules' is ready — {} record(s) found.", moduleCount);
            logger.info("Table 'history' is ready — {} record(s) found.", historyCount);
        } catch (Exception e) {
            logger.error("Failed to query table status: {}", e.getMessage(), e);
        }
    }
}
