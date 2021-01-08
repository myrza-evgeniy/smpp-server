package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.opensource.smppserver.dto.SessionWrapper;
import com.opensource.smppserver.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public class SessionHandler implements SmppServerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);

    private final AuthService authService;
    private final SessionStorage sessionStorage;

    @Autowired
    public SessionHandler(AuthService authService, SessionStorage sessionStorage) {
        this.authService = authService;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration, BaseBind bindRequest) throws SmppProcessingException {
        final boolean isAuthenticated = authService.isAuthenticated(sessionConfiguration, bindRequest);

        if (!isAuthenticated) {
            throw new SmppProcessingException(SmppConstants.STATUS_BINDFAIL);
        }
    }

    @Override
    public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) {
        final IncomingMessageHandler incomingMessageHandler = getIncomingMessageHandler();
        final SessionWrapper sessionWrapper = initSessionWrapper(session, incomingMessageHandler);

        sessionStorage.addSession(sessionId, sessionWrapper);

        incomingMessageHandler.setSession(session);
        session.serverReady(incomingMessageHandler);
    }


    @Override
    public void sessionDestroyed(Long sessionId, SmppServerSession session) {
        final SessionWrapper sessionWrapper = sessionStorage.getSessionById(sessionId);

        if (sessionWrapper != null) {
            try {
                if (sessionWrapper.getIncomingMessageHandler() != null && sessionWrapper.getSession() != null) {
                    sessionWrapper.getIncomingMessageHandler().freeUpResources();
                } else if (sessionWrapper.getSession() != null) {
                    sessionWrapper.getSession().destroy();
                }
            } catch (Exception e) {
                LOGGER.error("Error free up session {} resources. Reason: ", sessionId, e);
            } finally {
                sessionStorage.removeSessionById(sessionId);
            }
        }
    }

    /**
     * Inject prototype-scoped bean {@link IncomingMessageHandler} into a singleton bean {@link SessionHandler}.
     *
     * @return new prototype-scoped bean.
     */
    @Lookup
    public IncomingMessageHandler getIncomingMessageHandler() {
        return null;
    }


    // TODO: Need to update this javadoc
    /***
     * Creates new session wrapper with passed parameters.
     *
     * @param session The server session associated with the bind request and
     *      underlying channel.
     * @return new session wrapper.
     */
    private SessionWrapper initSessionWrapper(SmppServerSession session, IncomingMessageHandler incomingMessageHandler) {
        return SessionWrapper.builder()
                .session(session)
                .incomingMessageHandler(incomingMessageHandler)
                .build();
    }
}
