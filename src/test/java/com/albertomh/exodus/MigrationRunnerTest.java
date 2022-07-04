/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.albertomh.exodus.event.MigrationCompleteEventPublisher;
import com.albertomh.exodus.util.DatabaseUtils;

@TestInstance(Lifecycle.PER_CLASS)
public class MigrationRunnerTest {

    MigrationRunner runner;
    private DataSource dataSource;

    Logger logger = (Logger) LoggerFactory.getLogger(MigrationRunner.class);
    List<ILoggingEvent> logList;

    public MigrationRunnerTest() {
        dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    // ───── Utilities ─────────────────────────────────────────────────────────

    private ContextStartedEvent generateContextStartedEvent() {
        StaticApplicationContext staticApplicationContext = new StaticApplicationContext();
        return new ContextStartedEvent(staticApplicationContext);
    }

    // ───── Test lifecycle ────────────────────────────────────────────────────

    @BeforeEach
    private void beforeEach() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            statement.execute("DROP ALL OBJECTS;");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logger.detachAndStopAllAppenders();
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();
        logList = listAppender.list;
    }

    // ───── Tests ─────────────────────────────────────────────────────────────

    @Test
    public void testGetMigrationScripts() {
        Resource[] sqlScripts = MigrationRunner.getMigrationScripts();
        assertEquals(2, sqlScripts.length);
    }

    @Test
    public void testCreatingTheSchemaMigrationTable() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            runner = new MigrationRunner(dataSource);

            assertEquals(0, DatabaseUtils.countTables(statement));
            runner.createSchemaMigrationTable();
            assertEquals(1, DatabaseUtils.countTables(statement));
            assertEquals("exodus - Table `_schema_migration` has been created.", logList.get(0).getMessage());
            assertEquals(Level.INFO, logList.get(0).getLevel());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCSETriggersMigrationsWithBlankDatabase() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            runner = new MigrationRunner(dataSource);

            assertEquals(0, DatabaseUtils.countTables(statement));
            runner.onApplicationEvent(generateContextStartedEvent());
            assertEquals(2, DatabaseUtils.countTables(statement));

            assertEquals(4, logList.size());
            assertEquals("exodus - Table `_schema_migration` has been created.", logList.get(0).getMessage());
            assertEquals("exodus - Migration `already_applied_migration.sql` has been applied.", logList.get(1).getMessage());
            assertEquals("exodus - Migration `test_migration.sql` has been applied.", logList.get(2).getMessage());
            assertEquals("exodus - Ignored [0] existing migrations. Applied [2] new migrations.", logList.get(3).getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCSETriggersMigrationsWithPopulatedDatabase() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            TestingUtils.createSchemaMigrationTable(statement);
            TestingUtils.addRowToSchemaMigrationTable(statement, "already_applied_migration.sql");

            runner = new MigrationRunner(dataSource);

            assertEquals(1, DatabaseUtils.countTables(statement));
            runner.onApplicationEvent(generateContextStartedEvent());
            assertEquals(2, DatabaseUtils.countTables(statement));

            assertEquals(2, logList.size());
            assertEquals("exodus - Migration `test_migration.sql` has been applied.", logList.get(0).getMessage());
            assertEquals("exodus - Ignored [1] existing migration. Applied [1] new migration.", logList.get(1).getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMigrationsAreRecordedInMigrationsTable() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            TestingUtils.createSchemaMigrationTable(statement);
            TestingUtils.addRowToSchemaMigrationTable(statement, "already_applied_migration.sql");

            runner = new MigrationRunner(dataSource, migrationCompleteEventPublisher);
            runner.onApplicationEvent(generateContextStartedEvent());

            ArrayList<String> migrationFilenames = new ArrayList<>();
            ArrayList<String> migrationChecksums = new ArrayList<>();
            ResultSet result = statement.executeQuery("SELECT * FROM _schema_migration;");
            while (result.next()) {
                migrationFilenames.add(result.getString("file_name"));
                migrationChecksums.add(result.getString("checksum"));
            }
            assertEquals("already_applied_migration.sql, test_migration.sql", String.join(", ", migrationFilenames));
            assertEquals("BD0E7FC9C02332B837AB1E0877A959A3", migrationChecksums.get(1));
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

}