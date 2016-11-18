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
package com.indra.sofia2.ssap.kp;

import com.indra.sofia2.ssap.commadmessages.CommandMessageRequest;

/**
 * Interface to receice SIB notifications to Raw messages that are not SSAP compliant
 * @author jfgpimpollo
 *
 */
public interface Listener4SIBCommandMessageNotifications {
	
	/**
	 * API method to be invocated when the KP receive a raw message from SIB
	 * @param rawMessage
	 */
	void onCommandMessage(CommandMessageRequest commandMessage);

}
