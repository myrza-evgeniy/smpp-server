package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.opensource.smppserver.dto.SessionDto;
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
    public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) throws SmppProcessingException {
        final IncomingMessageHandler incomingMessageHandler = getIncomingMessageHandler();
        final SessionDto sessionDto = initSessionDto(sessionId, session);

        incomingMessageHandler.setSession(sessionDto);
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

    private SessionDto initSessionDto(Long sessionId, SmppServerSession session) {
        return SessionDto.builder()
                .sessionId(sessionId)
                .session(session)
                .systemId(session.getConfiguration().getSystemId())
                .build();
    }
}
