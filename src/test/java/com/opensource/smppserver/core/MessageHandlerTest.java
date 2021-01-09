package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MessageHandlerTest {

    private final MessageHandler messageHandler;

    @Autowired
    private MessageHandlerTest(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Test
    void testUnsupportedPduRequests() {
        PduResponse actualPduResponse;

        // cancelSm
        PduRequest<?> cancelSmRequest = new CancelSm();
        actualPduResponse = messageHandler.firePduRequestReceived((cancelSmRequest));
        Assertions.assertEquals(CancelSmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());

        // dataSm
        PduRequest<?> dataSmRequest = new DataSm();
        actualPduResponse = messageHandler.firePduRequestReceived((dataSmRequest));
        Assertions.assertEquals(DataSmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());

        // deliverSm
        PduRequest<?> deliverSmRequest = new DeliverSm();
        actualPduResponse = messageHandler.firePduRequestReceived((deliverSmRequest));
        Assertions.assertEquals(DeliverSmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());

        // querySm
        PduRequest<?> querySmRequest = new QuerySm();
        actualPduResponse = messageHandler.firePduRequestReceived((querySmRequest));
        Assertions.assertEquals(QuerySmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());

        // replaceSm
        PduRequest<?> replaceSmRequest = new ReplaceSm();
        actualPduResponse = messageHandler.firePduRequestReceived((replaceSmRequest));
        Assertions.assertEquals(ReplaceSmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());
    }
}