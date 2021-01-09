package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppServerSession;

public interface SessionDestroyListener {
    void destroy(Long sessionId, SmppServerSession session);
}
