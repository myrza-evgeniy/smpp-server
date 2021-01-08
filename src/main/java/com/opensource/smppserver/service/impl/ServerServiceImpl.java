package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.opensource.smppserver.config.ServerProperties;
import com.opensource.smppserver.core.SessionHandler;
import com.opensource.smppserver.service.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ServerServiceImpl implements ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerServiceImpl.class);

    private final ServerProperties serverProperties;
    private final SessionHandler sessionHandler;

    private DefaultSmppServer smppServer;
    private ScheduledExecutorService monitor;
    private ExecutorService executor;

    @Autowired
    public ServerServiceImpl(ServerProperties serverProperties, SessionHandler sessionHandler) {
        this.serverProperties = serverProperties;
        this.sessionHandler = sessionHandler;
    }

    @Override
    public void start() {
        configureSmppServer();

        try {
            smppServer.start();
        } catch (SmppChannelException e) {
            LOGGER.error("Failed to start smpp server", e);
        }
    }

    @Override
    public void stop() {
        smppServer.destroy();
        monitor.shutdown();
        executor.shutdown();
    }

    private void configureSmppServer() {
        SmppServerConfiguration smppServerConfiguration = new SmppServerConfiguration();
        smppServerConfiguration.setHost(serverProperties.getHost());
        smppServerConfiguration.setPort(serverProperties.getPort());
        smppServerConfiguration.setBindTimeout(serverProperties.getWaitBindTimeout());
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
