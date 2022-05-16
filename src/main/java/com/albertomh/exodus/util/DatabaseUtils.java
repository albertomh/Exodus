/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
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
     *
     */
    public static void applyMigration(Resource script) {
        // Generate an MD5 digest to uniquely identify each migration script.
        String scriptDigest = "";
        try {
            File file = script.getFile();
            byte[] scriptBytes = Files.readAllBytes(file.toPath());
            scriptDigest = DigestUtils.md5DigestAsHex(scriptBytes).toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Apply the migration script and update `_schema_migration`.
    }

}
