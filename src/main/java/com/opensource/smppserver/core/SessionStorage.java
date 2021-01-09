package com.opensource.smppserver.core;

public interface SessionStorage {
    void addSession(Long sessionId, SessionWrapper sessionWrapper);
    void removeSessionById(Long sessionId);
    SessionWrapper getSessionById(Long sessionId);
}
