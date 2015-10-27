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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SocketChannelReader extends Thread {
	
	private static final int BUFFER_CAPACITY = 255;

	private static Log log = LogFactory.getLog(SocketChannelReader.class);

	/** connection to server */
	private SocketChannel channel;

	/** selector to manage server connection */
	private Selector selector;

	/** indicates if running */
	private boolean running;
	
	/** the working buffer */
	private ByteBuffer readBuffer;

	/** The TcpipConnector */
	private TcpipConnector connector;
	
	/**
	 * constructor.
	 * @throws IOException If the selector can not be done
	 */
	public SocketChannelReader(TcpipConnector connector) throws IOException {
		super("SocketChannelReader");
		this.connector = connector;
		readBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
	
		this.selector = Selector.open();
	}

	public void setChannel(SocketChannel channel) throws ClosedChannelException {


			this.channel = channel;
			this.channel.register(selector, SelectionKey.OP_READ, new SSAPMessageParser());
		
	}
	
	/**
	 * The runnable method
	 */
	public void run() {

		running = true;
		while (running) {
			try {
				/**
				 * This method performs a blocking selection operation. 
				 * It returns only after at least one channel is selected, 
				 * this selector's wakeup method is invoked, or the current 
				 * thread is interrupted, whichever comes first.
				 * @return The number of keys, possibly zero, whose ready-operation sets were updated
				 */

				selector.select();
				Set<SelectionKey> readyKeys = selector.selectedKeys();

				Iterator<SelectionKey> i = readyKeys.iterator();
				while (i.hasNext()) {
					SelectionKey key = (SelectionKey) i.next();
					i.remove();
					SocketChannel channel = (SocketChannel) key.channel();
					SSAPMessageParser attachment = (SSAPMessageParser) key.attachment();

					try {
						long nbytes = channel.read(readBuffer);
						if (nbytes == -1) {
							channel.close();
							connector.getConnectionSupport().fireConnectionClosed(connector);
							return;
						}

						try {
							attachment.add(readBuffer);
							if (attachment.checkForMessages()) {
								Iterator<ClientMessage> msgList = attachment.getMessages();
								while (msgList.hasNext()) {
									//Logger.debug(this, "There is some message in the buffer");
									ClientMessage cm = msgList.next();
									connector.addIncomingMessage(cm);
								}
							}
							readBuffer.compact();
						} catch (IllegalArgumentException e) {
							log.error("illegal argument while parsing incoming event", e);
						}
					} catch (IOException ioe) {
						channel.close();
						connector.getConnectionSupport().fireConnectionClosed(connector);
					}
				}
			} catch (IOException ioe2) {
				log.error("error during select(): " + ioe2.getMessage());
				connector.getConnectionSupport().fireConnectionError(connector, ioe2.getMessage());
			} catch (Exception e) {
				log.error("exception during select()", e);
				connector.getConnectionSupport().fireConnectionError(connector, e.getMessage());
			}
		}
		log.debug("TCP/IP J2SE Connector SockeChannelReader ends");
	}

	public void shutdown() {
		running = false;
		// force the selector to unblock
		selector.wakeup();
	}
}
