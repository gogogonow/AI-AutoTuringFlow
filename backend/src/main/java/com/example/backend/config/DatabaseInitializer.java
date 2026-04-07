package com.example.backend.config;

import com.example.backend.repository.ModuleRepository;
import com.example.backend.repository.HistoryRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Database initializer that runs on application startup.
 * Verifies the database connection is healthy and logs table status.
 * As a CommandLineRunner, this runs after the full application context
 * (including Flyway migrations) is initialized.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private Flyway flyway;

    @Override
    public void run(String... args) {
        verifyDatabaseConnection();
        logFlywayMigrationStatus();
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

    private void logFlywayMigrationStatus() {
        try {
            MigrationInfoService info = flyway.info();
            MigrationInfo[] all = info.all();
            MigrationInfo current = info.current();

            logger.info("========================================");
            logger.info("Flyway Migration Status:");
            logger.info("Total migrations: {}", all.length);
            logger.info("Applied migrations: {}", info.applied().length);
            logger.info("Pending migrations: {}", info.pending().length);

            if (current != null) {
                logger.info("Current version: {} ({})",
                    current.getVersion(),
                    current.getDescription());
            }

            // Log any failed migrations
            MigrationInfo[] failed = info.failed();
            if (failed.length > 0) {
                logger.warn("FAILED MIGRATIONS DETECTED: {}", failed.length);
                for (MigrationInfo migration : failed) {
                    logger.warn("  - V{}: {} (State: {})",
                        migration.getVersion(),
                        migration.getDescription(),
                        migration.getState());
                }
            }

            logger.info("========================================");
        } catch (Exception e) {
            logger.error("Failed to query Flyway migration status: {}", e.getMessage(), e);
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
