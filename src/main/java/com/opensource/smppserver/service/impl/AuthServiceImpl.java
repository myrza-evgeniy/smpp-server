package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.opensource.smppserver.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public boolean isAuthenticated(SmppSessionConfiguration sessionConfiguration, BaseBind<?> bindRequest) {
        LOGGER.info("Accepted new bind request from customer with params: host:{}, systemId: {}, bind type: {}",
                sessionConfiguration.getHost(), sessionConfiguration.getSystemId(), sessionConfiguration.getType().name());

        final String systemId = bindRequest.getSystemId();
        final String password = bindRequest.getPassword();
        final SmppBindType bindType = sessionConfiguration.getType();

        return !bindType.equals(SmppBindType.RECEIVER);
    }
}