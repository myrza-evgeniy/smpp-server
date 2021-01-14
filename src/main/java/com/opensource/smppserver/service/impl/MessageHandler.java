package com.opensource.smppserver.service.impl;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.opensource.smppserver.service.MessageIdGenerator;
import com.opensource.smppserver.service.SessionDestroyListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Log4j2
public class MessageHandler extends DefaultSmppSessionHandler {

    private final SessionWrapper sessionWrapper;
    private final MessageIdGenerator messageIdGenerator;
    private final SessionDestroyListener sessionDestroyListener;
    private final ExecutorService msgExecutor;

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
                log.error("Current session state is not valid to continue communication.");
                return null;
            } else if (pduRequest == null) {
                log.error("Received null PDU request from customer {}.", sessionWrapper.getSystemId());
                return null;
            } else {
                log.error("Error while trying to handle incoming pdu request from customer {}.", sessionWrapper.getSystemId(), e);
                return getDefaultResponseForUnexpectedError(pduRequest);
            }
        }
    }

    private void onAcceptUnbind(Unbind unbind) {
        try {
            log.info("Accepted unbind request from customer {} in session {}", sessionWrapper.getSystemId(), sessionWrapper.getSessionId());
            sessionWrapper.getSession().sendResponsePdu(unbind.createResponse());
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            log.error("Error sending unbind_resp to customer {} in session {}. Reason: ", sessionWrapper.getSystemId(), sessionWrapper.getSessionId(), e);
        } finally {
            try {
                sessionDestroyListener.destroy(sessionWrapper.getSessionId(), sessionWrapper.getSession());
                msgExecutor.shutdown();
                msgExecutor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Shutdown msgExecutor interrupted because process timeout out");
            } finally {
                if (!msgExecutor.isTerminated()) {
                    log.info("Cancel non-finished tasks of msgExecutor");
                }
                msgExecutor.shutdownNow();
            }
        }
    }

    private void onAcceptEnquireLink(EnquireLink enquireLink) {
        try {
            log.debug("Accepted enquire_link from customer {} in session {} ", sessionWrapper.getSystemId(), sessionWrapper.getSystemId());
            sessionWrapper.getSession().sendResponsePdu(enquireLink.createResponse());
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            log.error("Error sending enquire_link_resp to customer {} in session {}. Reason: ", sessionWrapper.getSystemId(), sessionWrapper.getSessionId(), e);
        }
    }

    private void onAcceptSubmitSm(SubmitSm submitSm) {
        log.debug("Accepted submit_sm from customer {} in session {}", sessionWrapper.getSystemId(), sessionWrapper.getSessionId());
        final long messageId = this.messageIdGenerator.generateNext();
        final SubmitSmResp response = submitSm.createResponse();
        response.setMessageId(Long.toString(messageId));

        try {
            sessionWrapper.getSession().sendResponsePdu(response);
        } catch (UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException e) {
            log.error("Error sending enquire_link_resp to customer {} in session {}. Reason: ", sessionWrapper.getSystemId(), sessionWrapper.getSessionId(), e);
        }
    }

    private boolean isNotValidSessionState() {
        if (sessionWrapper == null || sessionWrapper.getSession() == null) {
            log.error("Unexpected critical situation - session is null.");
            return true;
        }

        if (!sessionWrapper.getSession().isBound()) {
            log.error("Unexpected critical situation - session is unbound.");
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
