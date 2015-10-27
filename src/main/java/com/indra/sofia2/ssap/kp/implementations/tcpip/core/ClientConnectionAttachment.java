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

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;


public abstract class ClientConnectionAttachment {
    
    /** Temporary buffer before it is converted into a message. */
    private byte[] buffer;
        
    /** List of ClientMessages already decoded. */
    private LinkedList<ClientMessage> decodedMessages;

	private int alloc;
	private int used;
	
	public ClientConnectionAttachment() {
		buffer = new byte[TCPIPConfiguration.BUFFER_SIZE];
		alloc = TCPIPConfiguration.BUFFER_SIZE;
		used = 0;

		decodedMessages = new LinkedList<ClientMessage>();
	}
	
	/** Check if there are enough bytes to build message(s). */
	public boolean checkForMessages() throws MessageParseException {
		int[] indexes = parseMessages(buffer);
		if(indexes == null) {
			return false;
		} else {
			if(indexes.length % 2 != 0) {
				throw new MessageParseException("The length of the indexes is no even.");
			}
			try {
				// Create client messages.
				for(int i = 0; i < indexes.length; i+=2) {
					int messageSize = indexes[i+1] - indexes[i] + 1;
					byte[] payload = new byte[messageSize];
					System.arraycopy(buffer, indexes[i], payload, 0, messageSize);
					ClientMessage cm = new ClientMessage(payload);
					decodedMessages.add(cm);
				}
				// Compact the buffer.
				int remaining = used - indexes[indexes.length-1] - 1;
				int newLength = remaining;
				if(newLength < TCPIPConfiguration.BUFFER_SIZE) {
					newLength = TCPIPConfiguration.BUFFER_SIZE;
				}
				byte[] newBuffer = new byte[newLength];
				System.arraycopy(buffer, indexes[indexes.length-1]+1, newBuffer, 0, remaining);
				buffer = newBuffer;
				alloc = newLength;
				used = remaining;
				return true;
			} catch(Exception e) {
				throw new MessageParseException(e.toString());
			}
		}	
	}
	
	/**
	 * 
	 * @return int[] Returns pairs of indexes indicating the start index (included) 
	 * and the last index (included) of the detected messages.
	 */
	public abstract int[] parseMessages(byte[] buffer);
		
	/**
	 * Indicates if there are pending messages.
	 * @return
	 */
	public boolean hasMessages() {
		return decodedMessages.size() > 0;
	}

	/**
	 * This method removes the messages returned each time is called.
	 * @return Iterator with the parsed messages.
	 */
	public Iterator<ClientMessage> getMessages() {
		if(hasMessages()) {
			LinkedList<ClientMessage> messages = new LinkedList<ClientMessage>();
			Iterator<ClientMessage> i = decodedMessages.iterator();
			while(i.hasNext()) {
				ClientMessage cm = i.next();
				i.remove();
				messages.add(cm);
			}
			return messages.iterator();
		} else {
			return null;
		}
	}
	
	/**
	 * Clear this array
	 */
	public void reset() { 
		used = 0; 
	}

	/**
	 * Returns the lenght of this array
	 */
	public int length() { 
		return used; 
	}
	
	/**
	 * Add a byte to this array.
	 * @param b byte to add
	 */
	public void add(byte b) { 
		buffer[used++] = b;
		if (used == alloc) {
			grow(TCPIPConfiguration.BUFFER_GROW);
		}
	}

	/**
	 * Add a array of bytes to this array
	 * @param b Byte array to add
	 * @param len Lenght of byte array to add.
	 */
	public void add(byte[] b, int len) {
		if (used+len >= alloc) {
			grow(len + TCPIPConfiguration.BUFFER_GROW);
		}
		System.arraycopy(b, 0, buffer, used, len);
		used += len;
	}
	/**
	 * Add a ByteArray to this array
	 * @param b ByteArray to add
	 */

	public void add(ByteBuffer bb) {
		bb.flip();
		if(used+bb.limit() >= alloc) {
			grow(bb.limit() + TCPIPConfiguration.BUFFER_GROW);
		}
		
		bb.get(buffer, used, bb.limit());
		//String s = new String(buffer);
		//Logger.debug("Incoming chunk of bytes read from SIB as follows: " + s);
		used += bb.limit();
	}

	private void grow(int newGap) {
		alloc += newGap;
		byte [] n = new byte[alloc];
		System.arraycopy(buffer, 0, n, 0, used);
		buffer = n;
	}
}
