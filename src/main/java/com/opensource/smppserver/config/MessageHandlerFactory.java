package com.opensource.smppserver.config;

import com.opensource.smppserver.core.MessageHandler;
import com.opensource.smppserver.core.SessionDestroyListener;
import com.opensource.smppserver.core.SessionWrapper;
import com.opensource.smppserver.service.MessageIdGenerator;
import com.opensource.smppserver.service.impl.CacheMessageIdGenerator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MessageHandlerFactory {

    @Bean
    public MessageIdGenerator cacheMessageIdGenerator() {
        return new CacheMessageIdGenerator();
    }

    @Bean(autowireCandidate = false)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MessageHandler getInstance(SessionWrapper sessionWrapper, SessionDestroyListener sessionDestroyListener) {
        return new MessageHandler(sessionWrapper, cacheMessageIdGenerator(), sessionDestroyListener);
    }
}
