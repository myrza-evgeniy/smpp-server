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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public class SessionHandler implements SmppServerHandler {

    private final AuthService authService;

    @Autowired
    public SessionHandler(AuthService authService) {
        this.authService = authService;
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
        final SessionWrapper sessionWrapper = initSessionWrapper(sessionId, session);

        incomingMessageHandler.setSessionWrapper(sessionWrapper);
        session.serverReady(incomingMessageHandler);
    }


    @Override
    public void sessionDestroyed(Long sessionId, SmppServerSession session) {
        // TODO: Add removing session
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

    /***
     * Creates new object of session with passed params.
     *
     * @param sessionId The unique numeric identifier assigned to the bind request.
     *      Will be the same value between sessionBindRequested, sessionCreated,
     *      and sessionDestroyed method calls.
     * @param session The server session associated with the bind request and
     *      underlying channel.
     * @return new object of session.
     */
    private SessionWrapper initSessionWrapper(Long sessionId, SmppServerSession session) {
        return SessionWrapper.builder()
                .sessionId(sessionId)
                .session(session)
                .systemId(session.getConfiguration().getSystemId())
                .build();
    }
}
