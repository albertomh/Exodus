/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.sql.SQLException;
import java.sql.Statement;


public class TestingUtils {

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

}
