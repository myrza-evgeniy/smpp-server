package com.opensource.smppserver.config;

import com.cloudhopper.smpp.SmppServerSession;
import com.opensource.smppserver.core.IncomingMessageHandler;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class IncomingMessageHandlerFactory {

    @Bean(autowireCandidate = false)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IncomingMessageHandler getInstance(SmppServerSession session) {
        return new IncomingMessageHandler(session);
    }
}
