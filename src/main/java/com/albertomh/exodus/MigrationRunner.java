/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.io.Resource;

@Component
class MigrationRunner implements ApplicationListener<ContextStartedEvent> {

    /**
     * Fetch all SQL scripts under `/db/migration/`.
     *
     * @return An array of Resources, each of them a script.
     */
    public static Resource[] getMigrationScripts() {
        Resource[] sqlScripts = null;
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
