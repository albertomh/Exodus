/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MigrationCompleteEventPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public MigrationCompleteEventPublisher() { }

    public void publishMigrationCompleteEvent(final String message) {
        MigrationCompleteEvent event = new MigrationCompleteEvent(this, message);
        eventPublisher.publishEvent(event);
    }

}
