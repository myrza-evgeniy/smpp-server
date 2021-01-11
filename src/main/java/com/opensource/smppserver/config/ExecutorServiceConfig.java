package com.opensource.smppserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ExecutorServiceConfig {

    @Value("${executor.pool.size.msg.handler:100}")
    private int msgExecutorPoolSize;

    @Bean(autowireCandidate = false)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ExecutorService msgExecutorPerSession() {
        return Executors.newFixedThreadPool(msgExecutorPoolSize);
    }

    @Bean
    public ExecutorService sessionExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public ScheduledExecutorService createMonitorExpiredRequests() {
        return Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private final AtomicInteger sequence = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("SmppCustomerSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });
    }
}
