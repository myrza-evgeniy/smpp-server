package com.opensource.smppserver.config;

import com.opensource.smppserver.core.IncomingMessageHandler;
import com.opensource.smppserver.core.SessionDestroyListener;
import com.opensource.smppserver.core.SessionWrapper;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class IncomingMessageHandlerFactory {

    @Bean(autowireCandidate = false)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IncomingMessageHandler getInstance(SessionWrapper sessionWrapper, SessionDestroyListener sessionDestroyListener) {
        return new IncomingMessageHandler(sessionWrapper, sessionDestroyListener);
    }
}
