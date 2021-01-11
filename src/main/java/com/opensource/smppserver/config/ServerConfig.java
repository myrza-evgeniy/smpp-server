package com.opensource.smppserver.config;

import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.opensource.smppserver.service.impl.SessionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ServerConfigProperties.class)
@RequiredArgsConstructor
public class ServerConfig {

    private final ExecutorServiceConfig executorServiceConfig;
    private final ServerConfigProperties serverConfigProperties;
    private final SessionHandler sessionHandler;

    @Bean
    public DefaultSmppServer getConfiguredSmppServer() {
        SmppServerConfiguration smppServerConfiguration = new SmppServerConfiguration();
        smppServerConfiguration.setHost(serverConfigProperties.getHost());
        smppServerConfiguration.setPort(serverConfigProperties.getPort());
        smppServerConfiguration.setBindTimeout(serverConfigProperties.getWaitBindTimeout());
        smppServerConfiguration.setDefaultRequestExpiryTimeout(serverConfigProperties.getRequestTimeout());
        smppServerConfiguration.setDefaultWindowMonitorInterval(2000);
        smppServerConfiguration.setDefaultWindowWaitTimeout(30000);
        smppServerConfiguration.setDefaultWindowSize(1000);

        return new DefaultSmppServer(smppServerConfiguration, sessionHandler,
                executorServiceConfig.sessionExecutor(), executorServiceConfig.createMonitorExpiredRequests());
    }
}
