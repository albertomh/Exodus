/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus.event;

import org.springframework.context.ApplicationEvent;

public class MigrationCompleteEvent extends ApplicationEvent {

    private String message;

    public MigrationCompleteEvent(
        Object source,
        String message
        ) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
