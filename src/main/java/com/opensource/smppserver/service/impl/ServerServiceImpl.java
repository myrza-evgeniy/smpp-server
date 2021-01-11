package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.opensource.smppserver.config.ServerConfigProperties;
import com.opensource.smppserver.core.SessionHandler;
import com.opensource.smppserver.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerServiceImpl.class);

    private final ServerConfigProperties serverConfigProperties;
    private final SessionHandler sessionHandler;

    private DefaultSmppServer smppServer;
    private ScheduledExecutorService monitor;
    private ExecutorService executor;

    @Override
    public void start() {
        configureSmppServer();

        try {
            smppServer.start();
        } catch (SmppChannelException e) {
            LOGGER.error("Failed to start smpp server. ", e);
        }
    }

    @Override
    public void stop() {
        try {
            smppServer.destroy();
        } catch (Exception e) {
            LOGGER.error("Failed to stop smpp server. ", e);
        } finally {
            monitor.shutdown();
            executor.shutdown();
        }
    }

    // TODO: Need to remake configure server
    private void configureSmppServer() {
        SmppServerConfiguration smppServerConfiguration = new SmppServerConfiguration();
        smppServerConfiguration.setHost(serverConfigProperties.getHost());
        smppServerConfiguration.setPort(serverConfigProperties.getPort());
        smppServerConfiguration.setBindTimeout(serverConfigProperties.getWaitBindTimeout());
        smppServerConfiguration.setDefaultRequestExpiryTimeout(4000);
        smppServerConfiguration.setDefaultWindowMonitorInterval(2000);
        smppServerConfiguration.setDefaultWindowWaitTimeout(30000);
        smppServerConfiguration.setDefaultWindowSize(1000);

        monitor = createMonitor();
        executor = Executors.newCachedThreadPool();

        smppServer = new DefaultSmppServer(smppServerConfiguration, sessionHandler, executor, monitor);
    }

    // TODO: Need to investigate incrementing this sequence
    private ScheduledExecutorService createMonitor() {
        return Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private final AtomicInteger sequence = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("SmppCustomerSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });
    }
}
