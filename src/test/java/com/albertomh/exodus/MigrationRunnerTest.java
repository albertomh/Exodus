/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.albertomh.exodus.util.DatabaseUtils;

public class MigrationRunnerTest {

    MigrationRunner runner;
    private DataSource dataSource;
    private Connection conn;
    private Statement statement;

    Logger logger = (Logger) LoggerFactory.getLogger(MigrationRunner.class);
    List<ILoggingEvent> logList;

    public MigrationRunnerTest() {
        dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ───── Test lifecycle ────────────────────────────────────────────────────

    @BeforeEach
    public void beforeEach() {
        try {
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
        assertEquals(1, sqlScripts.length);
    }

    @Test
    public void testCreatingTheSchemaMigrationTable() {
        runner = new MigrationRunner(dataSource);

        assertEquals(0, DatabaseUtils.countTables(statement));
        runner.createSchemaMigrationTable();
        assertEquals(1, DatabaseUtils.countTables(statement));
    }

    @Test
    public void testCSETriggersRunner() {
        runner = new MigrationRunner(dataSource);

        StaticApplicationContext staticApplicationContext = new StaticApplicationContext();
        ContextStartedEvent cse = new ContextStartedEvent(staticApplicationContext);

        runner.onApplicationEvent(cse);
        assertEquals(1, logList.size());
        assertEquals("exodus - Runner triggered by CSE.", logList.get(0).getMessage());
    }

}