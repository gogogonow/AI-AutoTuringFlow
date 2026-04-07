# Flyway Migration Guide

## Overview

This project uses Flyway for database schema migrations. Flyway automatically manages database schema changes by applying versioned SQL migration scripts in order.

## Migration Files Location

All migration files are located in: `backend/src/main/resources/db/migration/`

## Current Migrations

1. **V1__init_schema.sql** - Initial database schema with module and history tables
2. **V2__add_soft_delete_columns.sql** - Adds soft delete support (deleted flag and deleted_at timestamp)
3. **V3__add_full_field_support_for_optical_modules.sql** - Adds comprehensive optical module fields
4. **V4__add_version_batch_to_vendor_info.sql** - Adds version/batch tracking to vendor info
5. **V5__remove_vendor_and_status_from_module.sql** - Removes vendor and status from module table
6. **V6__add_serial_number_and_model_to_history.sql** - Adds serial number and model to history
7. **V7__remove_status_fields_from_history.sql** - Removes status fields from history
8. **V8__add_user_and_role_tables.sql** - Adds user authentication tables

## Automatic Repair Strategy

The application is configured with an automatic repair strategy that handles failed migrations:

### How It Works

1. On application startup, before running migrations, Flyway attempts to repair the schema history
2. The repair process:
   - Removes failed migration entries from the schema history table
   - Realigns checksums for existing migrations
   - Ensures the database is in a consistent state
3. After repair, normal migrations proceed

### Configuration

The automatic repair is implemented in `FlywayRepairConfig.java` and uses these settings in `application.properties`:

```properties
# Disable validation to allow repair to proceed
spring.flyway.validate-on-migrate=false

# Allow out-of-order migrations (useful for development/multi-branch work)
spring.flyway.out-of-order=true

# Baseline on migrate (creates baseline for existing databases)
spring.flyway.baseline-on-migrate=true
```

## Handling Failed Migrations

### Symptoms of a Failed Migration

You'll see an error like:
```
FlywayValidateException: Validate failed: Migrations have failed validation
Detected failed migration to version X (migration name).
Please remove any half-completed changes then run repair to fix the schema history.
```

### Automatic Resolution

With the current configuration, the application will automatically:
1. Detect the failed migration
2. Run `flyway.repair()` to fix the schema history
3. Re-attempt the migration

### Manual Resolution (if needed)

If automatic repair fails, you can manually repair using:

#### Option 1: Using Flyway Maven Plugin

```bash
cd backend
mvn flyway:repair
```

#### Option 2: Using Flyway CLI

```bash
flyway repair -url=jdbc:mysql://localhost:3306/optical_modules -user=root -password=password
```

#### Option 3: Direct Database Cleanup

1. Connect to the database
2. Check the schema history:
   ```sql
   SELECT * FROM flyway_schema_history WHERE success = 0;
   ```
3. Remove failed entries:
   ```sql
   DELETE FROM flyway_schema_history WHERE success = 0 AND version = 'X';
   ```
4. Restart the application

## Best Practices

### Creating New Migrations

1. **Naming Convention**: `V{version}__{description}.sql`
   - Example: `V9__add_location_table.sql`

2. **Versioning**: Use sequential integers (V1, V2, V3...)

3. **Idempotency**: Make migrations idempotent using conditional checks:
   ```sql
   -- Check if column exists before adding
   SET @dbname = DATABASE();
   SET @stmt = (SELECT IF(
       (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname
        AND TABLE_NAME = 'table_name'
        AND COLUMN_NAME = 'column_name') = 0,
       'ALTER TABLE table_name ADD COLUMN column_name VARCHAR(100)',
       'SELECT ''Column already exists'' AS message'
   ));
   PREPARE stmt FROM @stmt;
   EXECUTE stmt;
   DEALLOCATE PREPARE stmt;
   ```

4. **Testing**: Always test migrations on a local database first

5. **Rollback Plan**: Document how to rollback if needed (Flyway doesn't support automatic rollback)

### Development Workflow

1. Create migration file with next version number
2. Test locally with `mvn spring-boot:run`
3. Verify migration in `flyway_schema_history` table
4. Commit migration file to version control
5. Deploy to production

### Production Considerations

⚠️ **WARNING**: The automatic repair strategy is convenient for development but should be used carefully in production.

For production environments, consider:
1. Disabling automatic repair
2. Using manual repair procedures
3. Testing migrations on a staging environment first
4. Having a database backup before deploying migrations
5. Using Flyway Teams edition for additional safety features

## Troubleshooting

### Migration Checksum Mismatch

If you see checksum mismatches:
- The migration file content was changed after it was applied
- Run `flyway repair` to update checksums
- Never modify applied migration files

### Out-of-Order Migrations

With `spring.flyway.out-of-order=true`, Flyway allows applying older versions after newer ones. This is useful when:
- Merging feature branches with different migration versions
- Working in a team with multiple developers

### Migration Failed Halfway

The automatic repair will handle this, but to understand what happened:
1. Check application logs for the specific error
2. Manually verify which parts of the migration were applied
3. Check `flyway_schema_history` table for failed entries
4. The repair strategy will clean up and retry

## Monitoring

Check migration status at application startup in logs:
```
Flyway Migration Strategy: Attempting repair before migration
Flyway repair completed successfully
Flyway migrations completed successfully
```

## References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway Repair Command](https://flywaydb.org/documentation/command/repair)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
