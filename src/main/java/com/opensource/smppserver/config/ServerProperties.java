package com.opensource.smppserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("smpp.server")
public class ServerProperties {

    private String host;

    private int port;

    private String name;

    private String systemId;

    private int waitBindTimeout;

    private int requestTimeout;

    private int deliveryTimeOut = 2000;

    private int unbindTimeOut = 10;

    private long enquireResponseTimeout = 5000;

    private int monitorPoolSize = 5000;

    private int requestPoolSize = 2000;

    private long enquireIntervals = 5;

    private int attempts = 1;
}
