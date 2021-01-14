package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.opensource.smppserver.config.ServerConfig;
import com.opensource.smppserver.service.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ServerServiceImpl implements ServerService {

    private final ServerConfig serverConfig;
    private DefaultSmppServer smppServer;

    @Override
    public void start() {
        smppServer = serverConfig.getConfiguredSmppServer();

        try {
            smppServer.start();
        } catch (SmppChannelException e) {
            log.error("Failed to start smpp server. ", e);
        }
    }

    @Override
    public void stop() {
        try {
            smppServer.destroy();
        } catch (Exception e) {
            log.error("Failed to stop smpp server. ", e);
        }
    }
}
