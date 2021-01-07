package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IncomingMessageHandler extends DefaultSmppSessionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingMessageHandler.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(100);

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        switch (pduRequest.getCommandId()) {
            case SmppConstants.CMD_ID_SUBMIT_SM:
                executor.execute(() -> onAcceptSubmitSm((SubmitSm) pduRequest));
                return null;

            case SmppConstants.CMD_ID_ENQUIRE_LINK:
                executor.execute(() -> onAcceptEnquiredLink((EnquireLink) pduRequest));
                return null;

            case SmppConstants.CMD_ID_UNBIND:
                executor.execute(() -> onAcceptUnbind((Unbind) pduRequest));
                return null;

            default:
                PduResponse pduResponse = pduRequest.createResponse();
                pduResponse.setCommandStatus(SmppConstants.STATUS_INVCMDID);
                return pduResponse;
        }
    }

    private void onAcceptUnbind(Unbind unbind) {
        // TODO: Add handling request
    }

    private void onAcceptEnquiredLink(EnquireLink enquireLink) {
        // TODO: Add handling request
    }

    private void onAcceptSubmitSm(SubmitSm submitSm) {
        // TODO: Add handling request
    }
}
