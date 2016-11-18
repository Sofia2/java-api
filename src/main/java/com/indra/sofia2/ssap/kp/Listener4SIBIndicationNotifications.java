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

import com.indra.sofia2.ssap.ssap.SSAPMessage;

/**
 * Interfaz para recibir las notificaciones de suscripciones del Sib
 * @author jfgpimpollo
 *
 */
public interface Listener4SIBIndicationNotifications {
	
	/**
	 * Metodo que el API del kp-core invocará en el listener
	 * @param message
	 */
	void onIndication(String idNotifition,SSAPMessage message);

}
