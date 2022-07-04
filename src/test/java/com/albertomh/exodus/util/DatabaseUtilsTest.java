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

    public DatabaseUtilsTest() {
        dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
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
    }

    // ───── Tests ─────────────────────────────────────────────────────────────

    @Test
    public void testCountTablesWithEmptyDB() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            assertEquals(0, DatabaseUtils.countTables(statement));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCountTablesWithOneTable() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            TestingUtils.createSchemaMigrationTable(statement);

            assertEquals(1, DatabaseUtils.countTables(statement));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testlistAppliedMigrationsForEmptyDB() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            TestingUtils.createSchemaMigrationTable(statement);

            assertEquals(0, DatabaseUtils.listAppliedMigrations(statement).size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testlistAppliedMigrationsAfterMigrationsRun() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            TestingUtils.createSchemaMigrationTable(statement);
            TestingUtils.addRowToSchemaMigrationTable(statement);
            TestingUtils.addRowToSchemaMigrationTable(statement);

            assertEquals(2, DatabaseUtils.listAppliedMigrations(statement).size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testApplyMigration() {
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            TestingUtils.createSchemaMigrationTable(statement);
            assertEquals(1, DatabaseUtils.countTables(statement));
            assertEquals(0, DatabaseUtils.listAppliedMigrations(statement).size());

            Resource migrationFile = new ClassPathResource("db/migration/test_migration.sql");
            DatabaseUtils.applyMigration(dataSource, migrationFile);
            assertEquals(2, DatabaseUtils.countTables(statement));
            assertEquals(1, DatabaseUtils.listAppliedMigrations(statement).size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
