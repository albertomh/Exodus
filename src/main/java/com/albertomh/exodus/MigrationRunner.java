/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

import com.albertomh.exodus.event.MigrationCompleteEventPublisher;
import com.albertomh.exodus.util.DatabaseUtils;

@Component
public class MigrationRunner implements ApplicationListener<ContextStartedEvent> {

    Logger logger = LoggerFactory.getLogger(MigrationRunner.class);

    private DataSource dataSource;
    private MigrationCompleteEventPublisher migrationCompleteEventPublisher;

    public MigrationRunner(
        DataSource dataSource,
        MigrationCompleteEventPublisher migrationCompleteEventPublisher
        ) {
        this.dataSource = dataSource;
        this.migrationCompleteEventPublisher = migrationCompleteEventPublisher;
    }

    /**
     * Fetch all SQL scripts under `/db/migration/` and sort by filename.
     *
     * @return An array of Resources, each of them a script.
     */
    public static List<Resource> getMigrationScripts() {
        List<Resource> sqlScripts = new ArrayList<>();
        try {
            ClassLoader cl = MigrationRunner.class.getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);

            sqlScripts = Arrays.asList(resolver.getResources("classpath:/db/migration/**/*.sql"));
            Collections.sort(sqlScripts, Comparator.comparing(Resource::getFilename));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sqlScripts;
    }

    /**
     * Create the `_schema_migration` table if it does not yet exist.
     */
    public void createSchemaMigrationTable() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            String createMigrationsTableSQL = """
                CREATE TABLE IF NOT EXISTS _schema_migration (
                    id SERIAL PRIMARY KEY,
                    applied_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
                    file_name VARCHAR(255) NOT NULL UNIQUE,
                    checksum VARCHAR(128) NOT NULL
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
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            Integer tableCount = DatabaseUtils.countTables(statement);
            if (tableCount == 0) {
                createSchemaMigrationTable();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        List<Resource> sqlScripts = getMigrationScripts();

        // Loop over every SQL script and apply those that have not already been applied.
        Integer existingMigrationsCount = 0;
        Integer newMigrationsCount = 0;
        if (sqlScripts != null && dataSource != null) {
            ArrayList<String> appliedMigrations = new ArrayList<>();
            try (
                Connection conn = dataSource.getConnection();
                Statement statement = conn.createStatement();
            ) {
                appliedMigrations = DatabaseUtils.listAppliedMigrations(statement);
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }

            for (Resource script : sqlScripts) {
                if (appliedMigrations == null
                    || appliedMigrations.size() == 0
                    || (appliedMigrations.size() > 0 && !appliedMigrations.contains(script.getFilename()))) {
                    DatabaseUtils.applyMigration(dataSource, script);
                    newMigrationsCount++;
                    logger.info(String.format("exodus - Migration `%s` has been applied.", script.getFilename()));
                } else {
                    existingMigrationsCount++;
                }
            }
        }

        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            String existingMigPhrase = existingMigrationsCount == 1
                ? "existing migration"
                : "existing migrations";
            String newMigPhrase = newMigrationsCount == 1
                ? "new migration"
                : "new migrations";
            String message = String.format("Ignored [%d] %s. Applied [%d] %s.",
                existingMigrationsCount, existingMigPhrase,
                newMigrationsCount, newMigPhrase);
            logger.info(String.format("exodus - %s", message));

            migrationCompleteEventPublisher.publishMigrationCompleteEvent(message);

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

}
