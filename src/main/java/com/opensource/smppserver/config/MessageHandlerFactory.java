package com.opensource.smppserver.config;

import com.opensource.smppserver.service.impl.MessageHandler;
import com.opensource.smppserver.service.SessionDestroyListener;
import com.opensource.smppserver.service.impl.SessionWrapper;
import com.opensource.smppserver.service.MessageIdGenerator;
import com.opensource.smppserver.service.impl.CacheMessageIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@RequiredArgsConstructor
public class MessageHandlerFactory {

    private final ExecutorServiceConfig executorServiceConfig;

    @Bean
    public MessageIdGenerator cacheMessageIdGenerator() {
        return new CacheMessageIdGenerator();
    }

    @Bean(autowireCandidate = false)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MessageHandler getInstance(SessionWrapper sessionWrapper, SessionDestroyListener sessionDestroyListener) {
        return new MessageHandler(sessionWrapper, cacheMessageIdGenerator(), sessionDestroyListener, executorServiceConfig.msgExecutorPerSession());
    }
}
