package com.opensource.smppserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class ExecutorServiceConfig {

    @Value("${executor.pool.size.msg.handler:100}")
    private int msgExecutorPoolSize;

    @Bean(autowireCandidate = false)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ExecutorService msgExecutorPerSession() {
        return Executors.newFixedThreadPool(msgExecutorPoolSize);
    }

    @Bean(autowireCandidate = false)
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ExecutorService sessionExecutor() {
        return Executors.newCachedThreadPool();
    }
}
