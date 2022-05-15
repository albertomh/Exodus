/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus.util;

import java.sql.Statement;

public final class DatabaseUtils {

    private DatabaseUtils() { }

    /**
     * Count the number of tables in the 'public' schema.
     * @param statement
     * @return A count of existing tables.
     */
    public static Integer countTables(Statement statement) {
        Integer tableCount = -1;
        return tableCount;
    }

}
