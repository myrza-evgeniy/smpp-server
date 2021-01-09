package com.opensource.smppserver.config;

import com.cloudhopper.smpp.SmppServerSession;
import com.opensource.smppserver.core.IncomingMessageHandler;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionWrapper {
    private final SmppServerSession session;
    private final IncomingMessageHandler incomingMessageHandler;
}
