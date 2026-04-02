package com.example;

import com.example.config.WebConfigTest;
import com.example.controller.ProductControllerIntegrationTest;
import com.example.integration.SpringBoot3UpgradeIntegrationTest;
import com.example.model.ProductEntityTest;
import com.example.repository.ProductRepositoryTest;
import com.example.service.ProductServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Regression Test Suite for Spring Boot 3.2.0 Upgrade
 * 
 * This test suite validates that the upgrade from Spring Boot 2.7.14 to 3.2.0 maintains
 * backward compatibility and all existing functionality works correctly.
 * 
 * Key areas tested:
 * 1. Jakarta namespace migration (javax → jakarta)
 * 2. JPA/Hibernate with new MySQL connector
 * 3. REST API endpoints compatibility
 * 4. Validation annotations (Jakarta Bean Validation)
 * 5. JSON serialization/deserialization
 * 6. Transaction management
 * 7. CORS configuration
 * 8. Database operations
 * 
 * Run this test suite to verify the upgrade is successful:
 * mvn test -Dtest=RegressionTestSuite
 */
@Suite
@SelectClasses({
    ProductEntityTest.class,
    ProductServiceTest.class,
    ProductRepositoryTest.class,
    ProductControllerIntegrationTest.class,
    WebConfigTest.class,
    SpringBoot3UpgradeIntegrationTest.class,
    SpringBootRestApiApplicationTests.class
})
@DisplayName("Spring Boot 3.2.0 Upgrade Regression Test Suite")
public class RegressionTestSuite {
    // This class remains empty, it's used only as a holder for the above annotations
}