package com.example.backend.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway repair configuration that automatically repairs the schema history
 * when migration validation fails. This is useful for handling failed migrations
 * that left the database in an inconsistent state.
 *
 * WARNING: This configuration should be used carefully in production environments.
 * Consider using manual repair commands instead for production databases.
 */
@Configuration
public class FlywayRepairConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayRepairConfig.class);

    /**
     * Custom Flyway migration strategy that attempts to repair the schema history
     * before running migrations. This helps recover from failed migrations.
     */
    @Bean
    public FlywayMigrationStrategy repairStrategy() {
        return flyway -> {
            try {
                logger.info("========================================");
                logger.info("Flyway Migration Strategy: Attempting repair before migration");
                logger.info("========================================");

                // Attempt to repair the schema history
                // This will remove failed migration entries and re-align checksums
                flyway.repair();
                logger.info("Flyway repair completed successfully");

                // Now run the migrations
                flyway.migrate();
                logger.info("Flyway migrations completed successfully");

            } catch (Exception e) {
                logger.error("Flyway migration failed: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to complete database migrations", e);
            }
        };
    }
}
