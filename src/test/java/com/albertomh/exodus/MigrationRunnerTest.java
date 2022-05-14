/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;


public class MigrationRunnerTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    
    MigrationRunner runner;
    private DataSource dataSource;
    private Connection conn;
    private Statement statement;

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
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    // ───── Tests ─────────────────────────────────────────────────────────────

    @Test
    public void testGetMigrationScripts() {
        Resource[] sqlScripts = MigrationRunner.getMigrationScripts();
        assertEquals(1, sqlScripts.length);
    }

    @Test
    public void testCSETriggersRunner() {
        runner = new MigrationRunner(dataSource);

        StaticApplicationContext staticApplicationContext = new StaticApplicationContext();
        ContextStartedEvent cse = new ContextStartedEvent(staticApplicationContext);

        runner.onApplicationEvent(cse);
        assertEquals("Runner triggered by CSE.", outputStreamCaptor.toString().trim());
    }

}