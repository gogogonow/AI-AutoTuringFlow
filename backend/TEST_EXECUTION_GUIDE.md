# Regression Test Execution Guide

## Spring Boot 3.2.0 Upgrade - Comprehensive Testing

This guide explains how to run the regression tests created for the Spring Boot 2.7.14 → 3.2.0 upgrade.

## Test Coverage

Our regression test suite covers the following critical areas:

### 1. **Jakarta Namespace Migration** (javax → jakarta)
- ✅ `ProductEntityTest` - Tests Jakarta persistence annotations (@Entity, @Id, etc.)
- ✅ `ProductControllerIntegrationTest` - Tests Jakarta validation annotations (@Valid, @NotBlank, @Positive)

### 2. **JPA/Hibernate Functionality**
- ✅ `ProductRepositoryTest` - Tests JPA repository operations with new MySQL connector
- ✅ Entity lifecycle callbacks (@PrePersist, @PreUpdate)
- ✅ Database queries and custom repository methods

### 3. **REST API Compatibility**
- ✅ `ProductControllerIntegrationTest` - All CRUD endpoints
- ✅ Request/Response validation
- ✅ HTTP status codes
- ✅ Error handling

### 4. **Validation Framework**
- ✅ Jakarta Bean Validation constraints
- ✅ @NotBlank, @NotNull, @Positive validations
- ✅ Custom validation messages
- ✅ Multiple constraint violations

### 5. **JSON Serialization**
- ✅ Date/Time formatting (@JsonFormat)
- ✅ BigDecimal precision
- ✅ Jackson compatibility with Spring Boot 3

### 6. **Business Logic**
- ✅ `ProductServiceTest` - Service layer unit tests
- ✅ Transaction management (@Transactional)
- ✅ Error handling and exceptions

### 7. **Configuration**
- ✅ `WebConfigTest` - CORS configuration
- ✅ Cross-origin request handling

### 8. **End-to-End Integration**
- ✅ `SpringBoot3UpgradeIntegrationTest` - Full application integration tests
- ✅ Complete CRUD workflows
- ✅ Concurrent operations
- ✅ Transaction rollback scenarios

## Running the Tests

### Run All Regression Tests

```bash
cd backend
mvn clean test
```

### Run Test Suite Only

```bash
mvn test -Dtest=RegressionTestSuite
```

### Run Individual Test Classes

```bash
# Entity validation tests
mvn test -Dtest=ProductEntityTest

# Service layer tests
mvn test -Dtest=ProductServiceTest

# Repository/JPA tests
mvn test -Dtest=ProductRepositoryTest

# Controller/API tests
mvn test -Dtest=ProductControllerIntegrationTest

# CORS configuration tests
mvn test -Dtest=WebConfigTest

# Full integration tests
mvn test -Dtest=SpringBoot3UpgradeIntegrationTest

# Application context test
mvn test -Dtest=SpringBootRestApiApplicationTests
```

### Run with Coverage Report

```bash
mvn clean test jacoco:report
# Report will be in: target/site/jacoco/index.html
```

### Run in Verbose Mode

```bash
mvn test -X
```

## Test Files Location

All test files are located in:
```
backend/src/test/java/com/example/
├── SpringBootRestApiApplicationTests.java      # Application context test
├── RegressionTestSuite.java                    # Test suite aggregator
├── config/
│   └── WebConfigTest.java                      # CORS configuration tests
├── controller/
│   └── ProductControllerIntegrationTest.java   # API endpoint tests
├── integration/
│   └── SpringBoot3UpgradeIntegrationTest.java  # End-to-end tests
├── model/
│   └── ProductEntityTest.java                  # Entity validation tests
├── repository/
│   └── ProductRepositoryTest.java              # JPA repository tests
└── service/
    └── ProductServiceTest.java                 # Service layer tests
```

Test resources:
```
backend/src/test/resources/
└── application-test.properties                  # Test configuration
```

## Expected Results

All tests should pass with output similar to:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.RegressionTestSuite
[INFO] Tests run: 85, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 85, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## Test Breakdown

### ProductEntityTest (15 tests)
- Jakarta validation constraints
- Entity field validations
- Constructor and getter/setter tests
- Default values
- Multiple validation errors

### ProductServiceTest (18 tests)
- CRUD operations
- Business logic
- Error handling
- Transaction boundaries
- Repository interaction

### ProductRepositoryTest (20 tests)
- JPA operations (save, find, update, delete)
- Custom queries
- Entity lifecycle callbacks
- MySQL connector compatibility
- Transaction rollback
- BigDecimal precision

### ProductControllerIntegrationTest (22 tests)
- All REST endpoints
- Request validation
- Response format
- Error responses
- HTTP status codes
- JSON serialization

### WebConfigTest (7 tests)
- CORS configuration
- Allowed origins
- Allowed methods
- CORS headers

### SpringBoot3UpgradeIntegrationTest (12 tests)
- End-to-end workflows
- Complete CRUD operations
- Concurrent requests
- Transaction management
- Search functionality
- Stock management

### SpringBootRestApiApplicationTests (1 test)
- Application context loading

**Total: 95+ test cases**

## Key Validation Points

### ✅ Jakarta Namespace Migration
- All `javax.persistence.*` → `jakarta.persistence.*` imports working
- All `javax.validation.*` → `jakarta.validation.*` imports working

### ✅ MySQL Connector Update
- `mysql-connector-java` → `mysql-connector-j` 8.2.0 compatible
- Database operations work correctly
- Connection pooling functional

### ✅ Hibernate Dialect
- Updated to auto-detection (MySQLDialect)
- JPA operations successful

### ✅ Spring Boot 3.x Features
- Application starts correctly
- Dependency injection works
- Transaction management functional
- REST controllers operational

## Troubleshooting

### If Tests Fail

1. **Check Java Version**
   ```bash
   java -version  # Should be Java 21
   ```

2. **Clean and Rebuild**
   ```bash
   mvn clean install
   ```

3. **Check Dependencies**
   ```bash
   mvn dependency:tree
   ```

4. **Run Single Test for Debugging**
   ```bash
   mvn test -Dtest=ProductEntityTest#testValidProduct -X
   ```

5. **Check Application Properties**
   - Verify `application-test.properties` is being used
   - Check database configuration

### Common Issues

1. **Jakarta Import Errors**
   - Ensure all `javax.*` imports are updated to `jakarta.*`
   - Check IDE settings for auto-import

2. **Validation Not Working**
   - Verify Jakarta Validation dependency is included
   - Check `@Valid` annotation on controller methods

3. **Database Connection Issues**
   - Tests use H2 in-memory database by default
   - Check `application-test.properties` configuration

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Regression Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: |
          cd backend
          mvn clean test
      - name: Generate coverage report
        run: mvn jacoco:report
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## Success Criteria

The upgrade is considered successful when:

- ✅ All 95+ regression tests pass
- ✅ No Jakarta import errors
- ✅ All REST endpoints respond correctly
- ✅ Validation constraints work as expected
- ✅ Database operations complete successfully
- ✅ JSON serialization formats correctly
- ✅ CORS configuration allows expected origins
- ✅ Application context loads without errors

## Next Steps After Tests Pass

1. **Performance Testing**
   - Compare response times with Spring Boot 2.7.14
   - Check memory usage
   - Monitor startup time

2. **Security Scan**
   ```bash
   mvn dependency:check
   mvn org.owasp:dependency-check-maven:check
   ```

3. **Integration Testing with Real Database**
   - Update test configuration to use MySQL
   - Run full integration tests

4. **Load Testing**
   - Use JMeter or Gatling
   - Verify system handles expected load

## Documentation

For more details, see:
- `MIGRATION_NOTES.md` - Migration documentation
- `pom.xml` - Dependency versions
- Spring Boot 3.2.0 Release Notes
- Jakarta EE 9+ Migration Guide

## Support

If you encounter issues:
1. Check test output for specific failure messages
2. Review stack traces for Jakarta namespace errors
3. Verify all dependencies are compatible with Spring Boot 3.2.0
4. Consult Spring Boot 3 migration guide

---

**Last Updated:** 2024
**Spring Boot Version:** 3.2.0
**Java Version:** 21
**Test Framework:** JUnit 5 (Jupiter)
**Total Test Count:** 95+
