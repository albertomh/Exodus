/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.util.ArrayList;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.albertomh.exodus.util.DatabaseUtils;

@Component
class MigrationRunner implements ApplicationListener<ContextStartedEvent> {

    Logger logger = LoggerFactory.getLogger(MigrationRunner.class);

    private Connection conn;
    private Statement statement;

    MigrationRunner(DataSource dataSource) {
        // Initialise database connection and statement.
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Fetch all SQL scripts under `/db/migration/`.
     *
     * @return An array of Resources, each of them a script.
     */
    public static Resource[] getMigrationScripts() {
        Resource[] sqlScripts = null;
        try {
            ClassLoader cl = MigrationRunner.class.getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            sqlScripts = resolver.getResources("classpath:/db/migration/**/*.sql");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sqlScripts;
    }

    /**
     * Create the `_schema_migration` table if it does not yet exist.
     */
    public void createSchemaMigrationTable() {
        try {
            String createMigrationsTableSQL = """
                CREATE TABLE IF NOT EXISTS _schema_migration (
                    id SERIAL PRIMARY KEY,
                    applied_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
                    name VARCHAR(255) NOT NULL UNIQUE,
                    checksum TEXT NOT NULL
                );
                """;
            statement.execute(createMigrationsTableSQL);
            logger.info("exodus - Table `_schema_migration` has been created.");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Find any unapplied migrations on start-up and apply them.
     * 
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        // Initialise the `_schema_migration` table the first time Exodus runs.
        Integer tableCount = DatabaseUtils.countTables(statement);
        if (tableCount == 0) {
            createSchemaMigrationTable();
        }

        Resource[] sqlScripts = getMigrationScripts();

        // Loop over every SQL script and apply those that have not already been applied.
        Integer existingMigrationsCount = 0;
        Integer newMigrationsCount = 0;
        if (sqlScripts != null && conn != null && statement != null) {
            ArrayList<String> appliedMigrations = DatabaseUtils.listAppliedMigrations(statement);

            for (Resource script : sqlScripts) {
                if (appliedMigrations == null
                    || appliedMigrations.size() == 0
                    || (appliedMigrations.size() > 0 && !appliedMigrations.contains(script.getFilename()))) {
                    DatabaseUtils.applyMigration(conn, statement, script);
                    newMigrationsCount++;
                    logger.info(String.format("exodus - Migration `%s` has been applied.", script.getFilename()));
                } else {
                    existingMigrationsCount++;
                }
            }
        }

        try {
            String existingMigPhrase = existingMigrationsCount == 1
                ? "existing migration"
                : "existing migrations";
            String newMigPhrase = newMigrationsCount == 1
                ? "new migration"
                : "new migrations";
            logger.info(String.format("exodus - Ignored [%d] %s. Applied [%d] %s.",
                existingMigrationsCount, existingMigPhrase,
                newMigrationsCount, newMigPhrase));
            conn.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

}
