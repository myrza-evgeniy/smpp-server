package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IncomingMessageHandler extends DefaultSmppSessionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingMessageHandler.class);

    @Setter
    private SmppServerSession session;
    private final ExecutorService msgExecutor = Executors.newFixedThreadPool(100);
    private final AtomicLong messageId = new AtomicLong(0);

    @PreDestroy
    synchronized void freeUpResources() {
        try {
            if (session != null) {
                session.destroy();
                session = null;
            }

            if (!msgExecutor.isShutdown()) {
                msgExecutor.shutdown();
            }
        } catch (Exception e) {
            LOGGER.error("Error free up resources. ", e);
        }
    }

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        try {
            if (!isValidSessionState()) {
                return getDefaultResponseForUnexpectedError(pduRequest);
            }

            switch (pduRequest.getCommandId()) {
                case SmppConstants.CMD_ID_SUBMIT_SM:
                    msgExecutor.execute(() -> onAcceptSubmitSm((SubmitSm) pduRequest));
                    return null;

                case SmppConstants.CMD_ID_ENQUIRE_LINK:
                    msgExecutor.execute(() -> onAcceptEnquireLink((EnquireLink) pduRequest));
                    return null;

                case SmppConstants.CMD_ID_UNBIND:
                    msgExecutor.execute(() -> onAcceptUnbind((Unbind) pduRequest));
                    return null;

                default:
                    PduResponse pduResponse = pduRequest.createResponse();
                    pduResponse.setCommandStatus(SmppConstants.STATUS_INVCMDID);
                    return pduResponse;
            }
        } catch (Exception e) {
            if (session == null || !session.isBound()) {
                LOGGER.error("Current session state is not valid to continue communication.");
                return null;
            } else if (pduRequest == null) {
                LOGGER.error("Received null PDU request from customer {}.", session.getConfiguration().getSystemId());
                return null;
            } else {
                LOGGER.error("Error while trying to handle incoming pdu request from customer {}.", session.getConfiguration().getSystemId(), e);
                return getDefaultResponseForUnexpectedError(pduRequest);
            }
        }
    }

    private void onAcceptUnbind(Unbind unbind) {
        try {
            LOGGER.info("Accepted unbind request from {}", session.getConfiguration().getSystemId());
            session.sendResponsePdu(unbind.createResponse());

        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending unbind_resp to {}. Reason: ", session.getConfiguration().getSystemId(), e);
        } finally {
            freeUpResources();
        }
    }

    private void onAcceptEnquireLink(EnquireLink enquireLink) {
        try {
            LOGGER.debug("Accepted enquire_link from {}", session.getConfiguration().getSystemId());
            session.sendResponsePdu(enquireLink.createResponse());
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending enquire_link_resp to {}. Reason: ", session.getConfiguration().getSystemId(), e);
        }
    }

    private void onAcceptSubmitSm(SubmitSm submitSm) {
        final long messageId = this.messageId.incrementAndGet();
        final SubmitSmResp response = submitSm.createResponse();
        response.setMessageId(Long.toString(messageId));

        try {
            session.sendResponsePdu(response);
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending enquire_link_resp to {}. Reason: ", session.getConfiguration().getSystemId(), e);
        }
    }

    private boolean isValidSessionState() {
        if (session == null) {
            LOGGER.error("Unexpected critical situation - session is null.");
            return false;
        }

        if (!session.isBound()) {
            LOGGER.error("Unexpected critical situation - session is unbound.");
            return false;
        }
        return true;
    }

    private PduResponse getDefaultResponseForUnexpectedError(PduRequest<?> pduRequest) {
        PduResponse pduResponse = pduRequest.createResponse();
        pduResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
        return pduResponse;
    }
}
