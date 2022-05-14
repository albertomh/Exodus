/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

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

}
