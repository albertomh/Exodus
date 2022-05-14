/**
 * Copyright 2022 Alberto Morón Hernández
 */
package com.albertomh.exodus;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

public class MigrationRunnerTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    
    MigrationRunner runner;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testCSETriggersRunner() {
        runner = new MigrationRunner();

        StaticApplicationContext staticApplicationContext = new StaticApplicationContext();
        ContextStartedEvent cse = new ContextStartedEvent(staticApplicationContext);

        runner.onApplicationEvent(cse);
        assertEquals("Runner triggered by CSE.", outputStreamCaptor.toString().trim());
    }

}