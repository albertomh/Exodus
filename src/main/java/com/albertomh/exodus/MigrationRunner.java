/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;

@Component
class MigrationRunner implements ApplicationListener<ContextStartedEvent> {

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
