/*******************************************************************************
 * Copyright 2013-15 Indra Sistemas S.A.
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
package com.indra.sofia2.ssap.kp.implementations.tcpip.core;

import java.util.ArrayList;


/**
 * SSAPMessageParser.java
 * 
 * This class is responsible for detecting a SSAP message from the received string
 * of bytes. A SSAP message starts with the tag <SSAP_message> and
 * ends with </SSAP_message>
 * 
 * @author <a href="mailto:raul.otaolea@esi.es">Raul Otaolea</a>
 * @date 03/05/2010
 * @version 1.0
 */

public class SSAPMessageParser extends ClientConnectionAttachment {
	
	private final static String START_TOKEN = "<TCP_JSON>";
	private final static String END_TOKEN = "</TCP_JSON>";
	
	private ArrayList<Integer> indexes = new ArrayList<Integer>();
	
	public SSAPMessageParser() {
	}
	
	public int[] parseMessages(byte[] buffer) {
		
		String s;
		try {
			s = new String(buffer).toUpperCase();
		} catch(Exception e) {
			return null;
		}
		
		indexes.clear();
		int fromIndex = 0;
		int pos = 0;
		while(pos != -1) {
			pos = s.indexOf(SSAPMessageParser.START_TOKEN, fromIndex);
			if(pos != -1) {			
				// Start token found.
				fromIndex = pos;
				pos = s.indexOf(SSAPMessageParser.END_TOKEN, fromIndex + SSAPMessageParser.START_TOKEN.length());
				if(pos != -1) {
					indexes.add(fromIndex);
					indexes.add(pos + SSAPMessageParser.END_TOKEN.length()-1);
					fromIndex = pos + SSAPMessageParser.END_TOKEN.length();
				}
			}
		}
		
		if(indexes.size() == 0) {
			return null;
		} else {
			int[] idx = new int[indexes.size()];
			for(int i = 0; i < indexes.size(); i++) {
				idx[i] = indexes.get(i);
			}
			return idx;
		}
	}

}
