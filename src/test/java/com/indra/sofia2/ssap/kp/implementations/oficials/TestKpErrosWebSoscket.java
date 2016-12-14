package com.indra.sofia2.ssap.kp.implementations.oficials;

import org.atmosphere.wasync.Request.METHOD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.Kp;
import com.indra.sofia2.ssap.kp.KpToExtend;
import com.indra.sofia2.ssap.kp.config.WebSocketConnectionConfig;
import com.indra.sofia2.ssap.kp.implementations.oficials.abstracts.KpErrorsAbstract;
import com.indra.sofia2.ssap.kp.implementations.websockets.KpWebSocketClient;
import com.indra.sofia2.ssap.testutils.TestProperties;

public class TestKpErrosWebSoscket extends KpErrorsAbstract {
	@Override
	public KpToExtend getImplementation() {
		
		WebSocketConnectionConfig config=new WebSocketConnectionConfig();
		config.setEndpointUri(TestProperties.getInstance().get("test.officials.websockets.url"));
		config.setMethod(METHOD.GET);
		config.setSibConnectionTimeout(Integer.valueOf(TestProperties.getInstance().get("test.officials.websockets.connection_timeout")));
				
		return new KpWebSocketClient(config, KP, KP_INSTANCE, TOKEN);
	}

	@Override
	public Logger getLog() {
		return  LoggerFactory.getLogger(TestKpFunctionalWebSocket.class);
	}


}
