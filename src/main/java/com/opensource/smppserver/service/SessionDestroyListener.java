package com.opensource.smppserver.service;

import com.cloudhopper.smpp.SmppServerSession;

public interface SessionDestroyListener {
    void destroy(Long sessionId, SmppServerSession session);
}
