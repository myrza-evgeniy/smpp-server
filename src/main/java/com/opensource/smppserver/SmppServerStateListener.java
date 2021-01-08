package com.opensource.smppserver;

import com.opensource.smppserver.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmppServerStateListener {

    private final ServerService serverService;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationStarted() {
        serverService.start();
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationStopped() {
        serverService.stop();
    }
}
