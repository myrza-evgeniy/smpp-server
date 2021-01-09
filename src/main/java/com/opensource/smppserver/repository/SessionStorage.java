package com.opensource.smppserver.repository;

import com.opensource.smppserver.core.SessionWrapper;

public interface SessionStorage {
    void addSession(Long sessionId, SessionWrapper sessionWrapper);
    void removeSessionById(Long sessionId);
    SessionWrapper getSessionById(Long sessionId);
}
