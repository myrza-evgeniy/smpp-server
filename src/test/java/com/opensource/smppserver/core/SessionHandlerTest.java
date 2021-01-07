package com.opensource.smppserver.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SessionHandlerTest {

    private final ApplicationContext ctx;

    @Autowired
    private SessionHandlerTest(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Test
    void getIncomingMessageHandler() {
        SessionHandler first = ctx.getBean(SessionHandler.class);
        SessionHandler second = ctx.getBean(SessionHandler.class);

        assertEquals(first, second);
        assertNotEquals(first.getIncomingMessageHandler(), second.getIncomingMessageHandler());
    }
}