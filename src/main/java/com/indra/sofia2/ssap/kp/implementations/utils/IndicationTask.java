/*******************************************************************************
 * Copyright 2013-16 Indra Sistemas S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 ******************************************************************************/

package com.indra.sofia2.ssap.kp.implementations.utils;

import com.indra.sofia2.ssap.commadmessages.CommandMessageRequest;
import com.indra.sofia2.ssap.kp.Listener4SIBCommandMessageNotifications;
import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.ssap.SSAPMessage;

public class IndicationTask implements Runnable {
	private Listener4SIBIndicationNotifications indicationListener;
	private Listener4SIBCommandMessageNotifications commandListener;
	private String messageId;
	private SSAPMessage message;
	private CommandMessageRequest commandMessageRequest;

	public IndicationTask(
			Listener4SIBCommandMessageNotifications commandListener,
			CommandMessageRequest commandMessageRequest) {
		super();
		this.commandListener = commandListener;
		this.commandMessageRequest = commandMessageRequest;
	}

	public IndicationTask(
			Listener4SIBIndicationNotifications indicationListener,
			String messageId, SSAPMessage message) {
		super();
		this.indicationListener = indicationListener;
		this.messageId = messageId;
		this.message = message;
	}

	@Override
	public void run() {
		try {
			if (this.indicationListener != null) {
				this.indicationListener.onIndication(messageId, message);
			} else {
				this.commandListener.onCommandMessage(commandMessageRequest);
			}
		} catch(Throwable e){}
	}

}
