/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus.util;

import java.util.ArrayList;

import javax.sql.DataSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.util.DigestUtils;

public final class DatabaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

    private DatabaseUtils() { }

    /**
     * Count the number of tables in the 'public' schema.
     * @param statement
     * @return A count of existing tables.
     */
    public static Integer countTables(Statement statement) {
        Integer tableCount = 0;

        try {
            String tableCountSQL = """
                    SELECT count(*) AS \"table_count\"
                    FROM information_schema.tables
                    WHERE LOWER(table_schema) = 'public';""";
            ResultSet result = statement.executeQuery(tableCountSQL);
            while (result.next()) {
                tableCount = result.getInt("table_count");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return tableCount;
    }

    /**
     * @return An array of filenames of migrations that have been applied.
     */
    public static ArrayList<String> listAppliedMigrations(Statement statement) {
        ArrayList<String> appliedMigrations = new ArrayList<>();

        Integer tableCount = DatabaseUtils.countTables(statement);

        if (tableCount > 0) {
            try {
                String existingMigrationsSQL = "SELECT m.file_name FROM _schema_migration m;";
                ResultSet existingMigrations = statement.executeQuery(existingMigrationsSQL);
                while (existingMigrations.next()) {
                    appliedMigrations.add(existingMigrations.getString("file_name"));
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }

        return appliedMigrations;
    }

    /**
     * Apply the given migration, recording this in `_schema_migration`.
     *
     * @param dataSource
     * @param script The migration SQL script to apply.
     */
    public static void applyMigration(
        DataSource dataSource,
        Resource script
        ) {
        // Generate an MD5 digest to uniquely identify each migration script.
        String scriptDigest = "";
        try {
            File file = script.getFile();
            byte[] scriptBytes = Files.readAllBytes(file.toPath());
            scriptDigest = DigestUtils.md5DigestAsHex(scriptBytes).toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Apply the migration script and update `_schema_migration`.
        try (
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
        ) {
            ScriptUtils.executeSqlScript(conn, script);

            String updateSQL = "INSERT INTO _schema_migration(file_name, checksum) "
                    .concat(String.format("VALUES ('%s','%s');", script.getFilename(), scriptDigest));
            statement.executeUpdate(updateSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
