package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class IncomingMessageHandler extends DefaultSmppSessionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingMessageHandler.class);

    private final SessionWrapper sessionWrapper;
    private final SessionDestroyListener sessionDestroyListener;
    private final ExecutorService msgExecutor = Executors.newFixedThreadPool(100);
    private final AtomicLong messageId = new AtomicLong(0);

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        try {
            if (!isValidSessionState()) return getDefaultResponseForUnexpectedError(pduRequest);

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
            if (sessionWrapper == null || !sessionWrapper.getSession().isBound()) {
                LOGGER.error("Current session state is not valid to continue communication.");
                return null;
            } else if (pduRequest == null) {
                LOGGER.error("Received null PDU request from customer {}.", sessionWrapper.getSystemId());
                return null;
            } else {
                LOGGER.error("Error while trying to handle incoming pdu request from customer {}.", sessionWrapper.getSystemId(), e);
                return getDefaultResponseForUnexpectedError(pduRequest);
            }
        }
    }

    private void onAcceptUnbind(Unbind unbind) {
        try {
            LOGGER.info("Accepted unbind request from {}", sessionWrapper.getSystemId());
            sessionWrapper.getSession().sendResponsePdu(unbind.createResponse());
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending unbind_resp to {}. Reason: ", sessionWrapper.getSystemId(), e);
        } finally {
            sessionDestroyListener.destroy(sessionWrapper.getSessionId(), sessionWrapper.getSession());

            if (!msgExecutor.isShutdown()) {
                msgExecutor.shutdown();
            }
        }
    }

    private void onAcceptEnquireLink(EnquireLink enquireLink) {
        try {
            LOGGER.debug("Accepted enquire_link from {}", sessionWrapper.getSystemId());
            sessionWrapper.getSession().sendResponsePdu(enquireLink.createResponse());
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending enquire_link_resp to {}. Reason: ", sessionWrapper.getSystemId(), e);
        }
    }

    private void onAcceptSubmitSm(SubmitSm submitSm) {
        LOGGER.debug("Accepted submit_sm from {}", sessionWrapper.getSystemId());
        final long messageId = this.messageId.incrementAndGet();
        final SubmitSmResp response = submitSm.createResponse();
        response.setMessageId(Long.toString(messageId));

        try {
            sessionWrapper.getSession().sendResponsePdu(response);
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending enquire_link_resp to {}. Reason: ", sessionWrapper.getSystemId(), e);
        }
    }

    private boolean isValidSessionState() {
        if (sessionWrapper == null) {
            LOGGER.error("Unexpected critical situation - session is null.");
            return false;
        }

        if (!sessionWrapper.getSession().isBound()) {
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
