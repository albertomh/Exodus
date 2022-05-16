/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.util.UUID;
import java.sql.SQLException;
import java.sql.Statement;


public class TestingUtils {

    public static String generateRandomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void createSchemaMigrationTable(Statement statement) {
        try {
            String tableSchema = """
                CREATE TABLE IF NOT EXISTS _schema_migration (
                    id SERIAL PRIMARY KEY,
                    applied_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
                    name TEXT NOT NULL,
                    checksum TEXT NOT NULL
                );""";
            statement.execute(tableSchema);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addRowToSchemaMigrationTable(Statement statement, String migrationFilename) {
        try {
            String tableSchema = String.format(
                "INSERT INTO _schema_migration (name, checksum) VALUES ('%s', '%s');",
                migrationFilename, generateRandomString()
            );
            statement.execute(tableSchema);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addRowToSchemaMigrationTable(Statement statement) {
        addRowToSchemaMigrationTable(statement, generateRandomString());
    }

}
