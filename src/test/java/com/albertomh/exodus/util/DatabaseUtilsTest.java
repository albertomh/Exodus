/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.api.Assertions.*;

import com.albertomh.exodus.TestingUtils;

@TestInstance(Lifecycle.PER_CLASS)
public class DatabaseUtilsTest {

    DataSource dataSource;
    Connection conn;
    Statement statement;

    public DatabaseUtilsTest() {
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
    private void beforeEach() {
        try {
            statement.execute("DROP ALL OBJECTS;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ───── Tests ─────────────────────────────────────────────────────────────

    @Test
    public void testCountTablesWithEmptyDB() {
        assertEquals(0, DatabaseUtils.countTables(statement));
    }

    @Test
    public void testCountTablesWithOneTable() {
        TestingUtils.createSchemaMigrationTable(statement);

        assertEquals(1, DatabaseUtils.countTables(statement));
    }

    @Test
    public void testApplyMigration() {
        TestingUtils.createSchemaMigrationTable(statement);
        assertEquals(1, DatabaseUtils.countTables(statement));

        Resource migrationFile = new ClassPathResource("db/migration/test_migration.sql");
        DatabaseUtils.applyMigration(migrationFile);
        assertEquals(2, DatabaseUtils.countTables(statement));
    }

}
