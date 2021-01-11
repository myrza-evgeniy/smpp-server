package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.opensource.smppserver.config.ServerConfig;
import com.opensource.smppserver.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerServiceImpl.class);

    private final ServerConfig serverConfig;
    private DefaultSmppServer smppServer;

    @Override
    public void start() {
        smppServer = serverConfig.getConfiguredSmppServer();

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
        }
    }
}
