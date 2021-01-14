package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.opensource.smppserver.config.MessageHandlerFactory;
import com.opensource.smppserver.repository.SessionStorage;
import com.opensource.smppserver.service.AuthService;
import com.opensource.smppserver.service.SessionDestroyListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class SessionHandler implements SmppServerHandler {

    private final AuthService authService;
    private final SessionStorage sessionStorage;
    private final SessionDestroyListener sessionDestroyListener;
    private final MessageHandlerFactory messageHandlerFactory;

    @Override
    public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration, BaseBind bindRequest) throws SmppProcessingException {
        final boolean isAuthenticated = authService.isAuthenticated(sessionConfiguration, bindRequest);

        if (!isAuthenticated) {
            throw new SmppProcessingException(SmppConstants.STATUS_BINDFAIL);
        }
    }

    @Override
    public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) {
        final SessionWrapper sessionWrapper = initSessionWrapper(sessionId, session);
        final MessageHandler messageHandler = getMessageHandler(sessionWrapper);

        sessionStorage.addSession(sessionId, sessionWrapper);

        session.serverReady(messageHandler);
    }

    @Override
    public void sessionDestroyed(Long sessionId, SmppServerSession session) {
        log.info("Session {} of customer {} destroyed. Try to free up resources.", sessionId, session.getConfiguration().getSystemId());
        sessionDestroyListener.destroy(sessionId, session);
    }

    /**
     * Inject prototype-scoped bean {@link MessageHandler} into a singleton bean {@link SessionHandler}.
     *
     * @return new prototype-scoped bean.
     */
    public MessageHandler getMessageHandler(SessionWrapper sessionWrapper) {
        return messageHandlerFactory.getInstance(sessionWrapper, sessionDestroyListener);
    }


    // TODO: Need to update this javadoc
    /***
     * Creates new session wrapper with passed parameters.
     *
     * @param session The server session associated with the bind request and
     *      underlying channel.
     * @return new session wrapper.
     */
    private SessionWrapper initSessionWrapper(Long sessionId, SmppServerSession session) {
        return SessionWrapper.builder()
                .systemId(session.getConfiguration().getSystemId())
                .sessionId(sessionId)
                .session(session)
                .build();
    }
}
