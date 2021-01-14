package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.opensource.smppserver.service.AuthService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuthServiceImpl implements AuthService {

    @Override
    public boolean isAuthenticated(SmppSessionConfiguration sessionConfiguration, BaseBind<?> bindRequest) {
        log.info("Accepted new bind request from customer with params: host:{}, systemId: {}, bind type: {}",
                sessionConfiguration.getHost(), sessionConfiguration.getSystemId(), sessionConfiguration.getType().name());

        final String systemId = bindRequest.getSystemId();
        final String password = bindRequest.getPassword();
        final SmppBindType bindType = sessionConfiguration.getType();

        return !bindType.equals(SmppBindType.RECEIVER);
    }
}