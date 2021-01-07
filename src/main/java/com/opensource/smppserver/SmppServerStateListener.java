package com.opensource.smppserver;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmppServerStateListener {

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationStarted() {
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationStopped() {
    }
}
