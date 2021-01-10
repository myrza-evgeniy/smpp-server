package com.opensource.smppserver.service.impl;

import com.opensource.smppserver.service.MessageIdGenerator;

import java.util.concurrent.atomic.AtomicLong;

public class CacheMessageIdGenerator implements MessageIdGenerator {

    private final AtomicLong currentMessageId = new AtomicLong(0);

    @Override
    public long generateNext() {
        return currentMessageId.incrementAndGet();
    }
}
