/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.io.IOException;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import java.sql.SQLException;

import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Component
class MigrationRunner implements ApplicationListener<ContextStartedEvent> {

    private Connection conn;
    private Statement statement;

    MigrationRunner(DataSource dataSource) {
        // Initialise database connection and statement.
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Fetch all SQL scripts under `/db/migration/`.
     *
     * @return An array of Resources, each of them a script.
     */
    public static Resource[] getMigrationScripts() {
        Resource[] sqlScripts = null;
        try {
            ClassLoader cl = MigrationRunner.class.getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            sqlScripts = resolver.getResources("classpath:/db/migration/**/*.sql");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sqlScripts;
    }

    /**
     * Find any unapplied migrations on start-up and apply them.
     * 
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        System.out.println("Runner triggered by CSE.");
    }

}
