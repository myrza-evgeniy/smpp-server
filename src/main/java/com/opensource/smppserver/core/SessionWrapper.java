package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppServerSession;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionWrapper {
    private final SmppServerSession session;
    private final IncomingMessageHandler incomingMessageHandler;
}
