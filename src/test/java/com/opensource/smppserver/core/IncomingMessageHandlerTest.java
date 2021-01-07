package com.opensource.smppserver.core;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IncomingMessageHandlerTest {

    private final IncomingMessageHandler incomingMessageHandler;

    @Autowired
    private IncomingMessageHandlerTest(IncomingMessageHandler incomingMessageHandler) {
        this.incomingMessageHandler = incomingMessageHandler;
    }

    @Test
    void testExpectedNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            incomingMessageHandler.firePduRequestReceived(null);
        });
    }

    @Test
    void testSupportedPduRequests() {
        // supported pdu requests: enquireLink, submitSm, unbind
        Assertions.assertNull(incomingMessageHandler.firePduRequestReceived(new EnquireLink()));
        Assertions.assertNull(incomingMessageHandler.firePduRequestReceived(new SubmitSm()));
        Assertions.assertNull(incomingMessageHandler.firePduRequestReceived(new Unbind()));
    }

    @Test
    void testUnsupportedPduRequests() {
        PduResponse actualPduResponse;

        // cancelSm
        PduRequest<?> cancelSmRequest = new CancelSm();
        actualPduResponse = incomingMessageHandler.firePduRequestReceived((cancelSmRequest));
        Assertions.assertEquals(CancelSmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());

        // dataSm
        PduRequest<?> dataSmRequest = new DataSm();
        actualPduResponse = incomingMessageHandler.firePduRequestReceived((dataSmRequest));
        Assertions.assertEquals(DataSmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());

        // deliverSm
        PduRequest<?> deliverSmRequest = new DeliverSm();
        actualPduResponse = incomingMessageHandler.firePduRequestReceived((deliverSmRequest));
        Assertions.assertEquals(DeliverSmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());

        // querySm
        PduRequest<?> querySmRequest = new QuerySm();
        actualPduResponse = incomingMessageHandler.firePduRequestReceived((querySmRequest));
        Assertions.assertEquals(QuerySmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());

        // replaceSm
        PduRequest<?> replaceSmRequest = new ReplaceSm();
        actualPduResponse = incomingMessageHandler.firePduRequestReceived((replaceSmRequest));
        Assertions.assertEquals(ReplaceSmResp.class, actualPduResponse.getClass());
        Assertions.assertEquals(SmppConstants.STATUS_INVCMDID, actualPduResponse.getCommandStatus());
    }
}