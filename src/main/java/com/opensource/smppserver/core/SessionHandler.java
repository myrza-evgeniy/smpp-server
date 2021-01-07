package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public class SessionHandler implements SmppServerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);

    @Override
    public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration, BaseBind bindRequest) throws SmppProcessingException {
        // TODO: Add auth bind request
    }

    @Override
    public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) throws SmppProcessingException {
        // TODO: Add mapping new session with incoming massage handler
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
}
