package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.opensource.smppserver.service.MessageIdGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class MessageHandler extends DefaultSmppSessionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    private final SessionWrapper sessionWrapper;
    private final MessageIdGenerator messageIdGenerator;
    private final SessionDestroyListener sessionDestroyListener;
    private final ExecutorService msgExecutor = Executors.newFixedThreadPool(100);

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        try {
            if (isNotValidSessionState()) return null;

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
            if (isNotValidSessionState()) {
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
            LOGGER.info("Accepted unbind request in session {} from customer {}", sessionWrapper.getSystemId(), sessionWrapper.getSystemId());
            sessionWrapper.getSession().sendResponsePdu(unbind.createResponse());
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending unbind_resp to {}. Reason: ", sessionWrapper.getSystemId(), e);
        } finally {
            try {
                sessionDestroyListener.destroy(sessionWrapper.getSessionId(), sessionWrapper.getSession());
                msgExecutor.shutdown();
                msgExecutor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.warn("Shutdown msgExecutor interrupted because process timeout out");
            } finally {
                if (!msgExecutor.isTerminated()) {
                    LOGGER.info("Cancel non-finished tasks of msgExecutor");
                }
                msgExecutor.shutdownNow();
            }
        }
    }

    private void onAcceptEnquireLink(EnquireLink enquireLink) {
        try {
            LOGGER.debug("Accepted enquire_link in session {} from customer {}", sessionWrapper.getSystemId(), sessionWrapper.getSystemId());
            sessionWrapper.getSession().sendResponsePdu(enquireLink.createResponse());
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending enquire_link_resp to {}. Reason: ", sessionWrapper.getSystemId(), e);
        }
    }

    private void onAcceptSubmitSm(SubmitSm submitSm) {
        LOGGER.debug("Accepted submit_sm in session {} from customer {}", sessionWrapper.getSystemId(), sessionWrapper.getSystemId());
        final long messageId = this.messageIdGenerator.generateNext();
        final SubmitSmResp response = submitSm.createResponse();
        response.setMessageId(Long.toString(messageId));

        try {
            sessionWrapper.getSession().sendResponsePdu(response);
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            LOGGER.error("Error sending enquire_link_resp to {}. Reason: ", sessionWrapper.getSystemId(), e);
        }
    }

    private boolean isNotValidSessionState() {
        if (sessionWrapper == null || sessionWrapper.getSession() == null) {
            LOGGER.error("Unexpected critical situation - session is null.");
            return true;
        }

        if (!sessionWrapper.getSession().isBound()) {
            LOGGER.error("Unexpected critical situation - session is unbound.");
            return true;
        }
        return false;
    }

    private PduResponse getDefaultResponseForUnexpectedError(PduRequest<?> pduRequest) {
        PduResponse pduResponse = pduRequest.createResponse();
        pduResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
        return pduResponse;
    }
}
