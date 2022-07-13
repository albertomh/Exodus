/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.util.List;
import java.util.stream.Collectors;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

    private DataSource dataSource;
    private MigrationRunner runner;
    private MigrationCompleteEventPublisher migrationCompleteEventPublisher;

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
    private void setUp() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            statement.execute("DROP ALL OBJECTS;");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        migrationCompleteEventPublisher = mock(MigrationCompleteEventPublisher.class);
        runner = new MigrationRunner(dataSource, migrationCompleteEventPublisher);

        logger.detachAndStopAllAppenders();
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();
        logList = listAppender.list;
    }

    // ───── Tests ─────────────────────────────────────────────────────────────

    @Test
    public void testGetMigrationScripts() {
        List<Resource> sqlScripts = MigrationRunner.getMigrationScripts();

        List<String> filenames = sqlScripts.stream().map(s -> s.getFilename()).collect(Collectors.toList());
        assertEquals("1970-01-01_09.00__auth__create-user.sql, 1970-02-01_10.00__email__create-email.sql", String.join(", ", filenames));
        assertEquals(2, sqlScripts.size());
    }

    @Test
    public void testCreatingTheSchemaMigrationTable() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            Integer initialTableCount = DatabaseUtils.countTables(statement);

            runner.createSchemaMigrationTable();

            assertEquals(0, initialTableCount);
            assertEquals(1, DatabaseUtils.countTables(statement));
            assertEquals(Level.INFO, logList.get(0).getLevel());
            assertEquals("exodus - Table `_schema_migration` has been created.", logList.get(0).getMessage());
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
            Integer initialTableCount = DatabaseUtils.countTables(statement);

            runner.onApplicationEvent(generateContextStartedEvent());

            assertEquals(0, initialTableCount);
            assertEquals(3, DatabaseUtils.countTables(statement));
            assertEquals(4, logList.size());
            assertEquals("exodus - Table `_schema_migration` has been created.", logList.get(0).getMessage());
            assertEquals("exodus - Migration `1970-01-01_09.00__auth__create-user.sql` has been applied.", logList.get(1).getMessage());
            assertEquals("exodus - Migration `1970-02-01_10.00__email__create-email.sql` has been applied.", logList.get(2).getMessage());
            String message = "Ignored [0] existing migrations. Applied [2] new migrations.";
            assertEquals(String.format("exodus - %s", message), logList.get(3).getMessage());
            verify(migrationCompleteEventPublisher).publishMigrationCompleteEvent(message);
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
            TestingUtils.addRowToSchemaMigrationTable(statement, "1970-01-01_09.00__auth__create-user.sql");
            Integer initialTableCount = DatabaseUtils.countTables(statement);

            runner.onApplicationEvent(generateContextStartedEvent());

            assertEquals(1, initialTableCount);
            assertEquals(2, DatabaseUtils.countTables(statement));
            assertEquals(2, logList.size());
            assertEquals("exodus - Migration `1970-02-01_10.00__email__create-email.sql` has been applied.", logList.get(0).getMessage());
            String message = "Ignored [1] existing migration. Applied [1] new migration.";
            assertEquals(String.format("exodus - %s", message), logList.get(1).getMessage());
            verify(migrationCompleteEventPublisher).publishMigrationCompleteEvent(message);
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

            runner.onApplicationEvent(generateContextStartedEvent());

            ArrayList<String> migrationFilenames = new ArrayList<>();
            ArrayList<String> migrationChecksums = new ArrayList<>();
            ResultSet result = statement.executeQuery("SELECT * FROM _schema_migration;");
            while (result.next()) {
                migrationFilenames.add(result.getString("file_name"));
                migrationChecksums.add(result.getString("checksum"));
            }
            assertEquals("1970-01-01_09.00__auth__create-user.sql, 1970-02-01_10.00__email__create-email.sql", String.join(", ", migrationFilenames));
            assertEquals("F7CAE56B9AD4136BE9530CB25979F397", migrationChecksums.get(1));
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

}