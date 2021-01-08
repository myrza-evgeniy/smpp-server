package com.opensource.smppserver.service;

import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;

public interface AuthService {

    boolean isAuthenticated(SmppSessionConfiguration sessionConfiguration, BaseBind<?> bindRequest);
}
