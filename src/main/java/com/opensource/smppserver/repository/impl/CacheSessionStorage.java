package com.opensource.smppserver.repository.impl;

import com.opensource.smppserver.service.impl.SessionWrapper;
import com.opensource.smppserver.repository.SessionStorage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheSessionStorage implements SessionStorage {

    /**
     * key - unique sessionId, that generated by cloudhopper library.
     * value - wrapper object that contains managed session object and incoming request handler.
     */
    private final Map<Long, SessionWrapper> sessions = new ConcurrentHashMap<>();

    @Override
    public void addSession(Long sessionId, SessionWrapper sessionWrapper) {
        sessions.putIfAbsent(sessionId, sessionWrapper);
    }

    @Override
    public void removeSessionById(Long sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public SessionWrapper getSessionById(Long sessionId) {
        return sessions.get(sessionId);
    }
}
