package com.opensource.smppserver.dto;

import com.cloudhopper.smpp.SmppServerSession;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionWrapper {
    private final Long sessionId;
    private final String systemId;
    private final SmppServerSession session;
}
