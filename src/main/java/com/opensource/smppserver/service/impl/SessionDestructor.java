package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppServerSession;
import com.opensource.smppserver.repository.SessionStorage;
import com.opensource.smppserver.service.SessionDestroyListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionDestructor implements SessionDestroyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionDestructor.class);

    private final SessionStorage sessionStorage;

    @Override
    public synchronized void destroy(Long sessionId, SmppServerSession session) {
        final SessionWrapper sessionWrapper = sessionStorage.getSessionById(sessionId);

        try {
            if (sessionWrapper != null) {
                if (sessionWrapper.getSession() != null) {
                    sessionWrapper.getSession().destroy();
                }

                sessionStorage.removeSessionById(sessionId);
            } else {
                LOGGER.error("Session {} of customer {} not found in the session storage", sessionId, session.getConfiguration().getSystemId());
                session.destroy();
            }
        } catch (Exception e) {
            if (sessionWrapper != null) sessionStorage.removeSessionById(sessionId);
            LOGGER.error("Destroy session {} of customer {} failed. ", sessionId, session.getConfiguration().getSystemId(), e);
        }
    }
}
