package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppServerSession;
import com.opensource.smppserver.repository.SessionStorage;
import com.opensource.smppserver.service.SessionDestroyListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class SessionDestructor implements SessionDestroyListener {

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
                log.error("Session {} of customer {} not found in the session storage", sessionId, session.getConfiguration().getSystemId());
                session.destroy();
            }
        } catch (Exception e) {
            if (sessionWrapper != null) sessionStorage.removeSessionById(sessionId);
            log.error("Destroy session {} of customer {} failed. ", sessionId, session.getConfiguration().getSystemId(), e);
        }
    }
}
