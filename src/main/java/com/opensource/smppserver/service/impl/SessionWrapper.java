package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppServerSession;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionWrapper {
    private final String systemId;
    private final Long sessionId;
    private final SmppServerSession session;
}
