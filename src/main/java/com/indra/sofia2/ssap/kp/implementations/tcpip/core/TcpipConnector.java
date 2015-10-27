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
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.indra.sofia2.ssap.kp.implementations.tcpip.connector.AbstractConnector;
import com.indra.sofia2.ssap.kp.implementations.tcpip.connector.ConnectionSupport;
import com.indra.sofia2.ssap.kp.implementations.tcpip.exception.KPIConnectorException;



public class TcpipConnector extends AbstractConnector implements Runnable {
	
	private static Log log = LogFactory.getLog(TcpipConnector.class);

	//host is maintained to keep backwards compatibility <=2.0.3 release
	public static final String HOST = "HOST";
	public static final String DEFAULT_IPADDRESS = "DEFAULT_IPADDRESS";
	public static final String PORT = "PORT";
	private static final String KEEP_ALIVE = "KEEP_ALIVE";
	/** This is used only for testing purpose, connecting to NOKIA SIB, should not be used otherwise */
	private static final boolean NOKIA_SIB = false;
	
	private Thread thread;
	
	/** Connection to server */
	private SocketChannel channel;

	/** Queue for incoming events */
	private LinkedBlockingQueue<ClientMessage> inQueue;
	
	/** Queue for outgoing events */
	private LinkedBlockingQueue<ClientMessage> outQueue;

	/** Lock to avoid expensive run() */
	private Object lock;
	
	/** Reference to NIOEventReader that reads events from the server */
	private SocketChannelReader reader;

	/** Buffer for outgoing events */
	private ByteBuffer writeBuffer;

	/** Still running? */
	private boolean running;
	
    public TcpipConnector() throws IOException {
    	super();
		this.inQueue = new LinkedBlockingQueue<ClientMessage>();
		this.outQueue = new LinkedBlockingQueue<ClientMessage>();
		this.writeBuffer = ByteBuffer.allocate(TCPIPConfiguration.BUFFER_SIZE);
		this.lock = new Object();
		
		// start the reader (without a channel)
		this.reader = new SocketChannelReader(this);
		
		// start processing incoming/outgoing messages.
		this.thread = new Thread(this, "TcpipConnector");
		this.thread.start();
    }
    
    
	/** 
	 * This is the threadeable method. Checks if there are pending messages in the output and input queues and
	 * process them. Waiting condition is whether any of the queues have at least a message
	 * 
	 * @return	None
	 */
	public void run() {
		running = true;
		while(running) {
			try {
				if(inQueue.size() == 0 && outQueue.size() == 0) {
					synchronized(lock) {
						lock.wait();
					}
				}
				//Check if we have to reconnect, only when KEEP_ALIVE is set to false
				if (!this.keepAlive){
					connect();
				}
				processIncomingMessages();
				writeOutgoingMessages();
				//Ask if we should close the connection once the queue has been processed
				//Check if we have to reconnect, only when KEEP_ALIVE is set to false
				if (!this.keepAlive){
					disconnect();
				}
				
			} catch (KPIConnectorException cex) {
				cex.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.debug("Client TCP/IP connector ends");
	}

	/** 
	 * Handle incoming messages from the inQueue.
	 * 
	 * @return	None
	 * 
	 */
	protected void processIncomingMessages() throws KPIConnectorException {
		try {
			if(inQueue.size() > 0) {
			
				ClientMessage msg;
				while (inQueue.size() > 0) {
					msg = inQueue.take();
					log.debug("Received: " + msg);
					this.messageListeners.fireMessageReceived(msg.getPayload());
				}	

			}
		} catch(InterruptedException iex) {
			iex.printStackTrace();
			throw new KPIConnectorException("The socket is interrupted: " + iex.getMessage(), iex);
		}
	}
	
	
	/** 
	 * Write all events waiting in the outQueue.
	 * 
	 * @return	None
	 * 
	 */
	private void writeOutgoingMessages() throws KPIConnectorException {
		try {
			
			ClientMessage msg;
			if(outQueue.size() > 0) {
		
				while (outQueue.size() > 0) {
					msg = outQueue.take();
					long time = System.currentTimeMillis();
					
					writeMessage(msg);
					
					log.debug(new StringBuilder("Sent message in ")
						.append((System.currentTimeMillis()-time))
						.append(" ms: ")
						.append(msg).toString());
				}
			}
		} catch (KPIConnectorException cex) {
			throw new KPIConnectorException("Cannot write an outgoing message: " + cex.getMessage(), cex);
		} catch (InterruptedException iex) {
			throw new KPIConnectorException("The socket is interrupted: " + iex.getMessage(), iex);
		}
	}

	/**
	 * Connects to a SIB using TCP/IP
	 * 
	 * @return	None
	 */
	public synchronized void connect() throws KPIConnectorException {
		if(this.channel != null && this.channel.isConnected()) {
			return;
		}
		try {
			//set the running flag to true
			this.running=true;
			String host = this.properties.getProperty(TcpipConnector.HOST);
			String defaultIPAddress = this.properties.getProperty(TcpipConnector.DEFAULT_IPADDRESS);
			int port = Integer.parseInt(this.properties.getProperty(TcpipConnector.PORT));
			//This is done to keep backwards compatibility with previous KP that did not specifically set the KEEP_ALIVe
			//flag when setting the connector properties. 
			boolean keep_alive = Boolean.parseBoolean(this.properties.getProperty(TcpipConnector.KEEP_ALIVE));
			//Keep Alive equals false should only occuer when the NOKIA_SIB flag is set to true, meaning that our KP is connecting
			//to NOKIA's SIB, otherwise will always be TRUE cuz otherwise this KP won't work well with our SIB
			if (TcpipConnector.NOKIA_SIB && !keep_alive){
				this.keepAlive = false;
			}
			else{
				this.keepAlive = true;
			}
			
			InetSocketAddress inetAddress;
			
			//this is done to keep backward compatibility prior 2.0.4 release
			if (host == null || host.isEmpty()) {
				//check if defaultIp is also empty
				if (defaultIPAddress == null || defaultIPAddress.isEmpty()) {
					throw new UnknownHostException();
				} else {
					//otherwise start listening on defaultIp
					inetAddress = new InetSocketAddress(defaultIPAddress, port);
				}
			}
			//start listening on host
			else
			{
				inetAddress = new InetSocketAddress(host, port);
			}
				
			// open the socket channel
			this.channel = SocketChannel.open(inetAddress);
			this.channel.configureBlocking(false);
			this.channel.socket().setTcpNoDelay(true);
			
			//Check if the reader channel is not null, may happen if a disconnect has ocurred
			if (this.reader == null) {
				this.reader = new SocketChannelReader(this);
			}
			
			// assign the channel to the reader.
			this.reader.setChannel(this.channel);
			this.reader.start();
			
		} catch (UnknownHostException ex) {
			throw new KPIConnectorException("The host is unknown: " + ex.getMessage(), ex);
		} catch (SocketException ex) {
			throw new KPIConnectorException("There was an error in TCP/IP connection:" + ex.getMessage(), ex);
		} catch (ClosedChannelException ex) {
			throw new KPIConnectorException("The socket channel was closed: " + ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new KPIConnectorException("There was an I/O exception in openning/using the socket channel: " + ex.getMessage(), ex);
		} catch(Exception e) {
			e.printStackTrace();
			throw new KPIConnectorException(e.getMessage(), e);
		}
	}
	
	/**
	 * Disconnect the client stop our readers and close the channel.
	 * 
	 * @return	None
	 * 
	 */
	public void disconnect() throws KPIConnectorException {

		if(channel == null) {
			return;
		}
		
		try {
			channel.close();
			channel = null;
			//Call destroy method so the SocketChannelReader and the TcpIpConnector threads are terminated gracefully
			
			destroy();
		} catch (IOException ex) {
			throw new KPIConnectorException("There was an I/O exception in openning/using the socket channel: " + ex.getMessage(), ex);
		}
	}    

	/** 
	 * Send a message to the server.
	 * 
	 * @param	cm		A ClientMessage object representing the message to be sent to the SIB
	 * @return	None
	 */
	private void writeMessage(ClientMessage cm) throws KPIConnectorException {
		try {
			// Calculate how many chunks are needed to send whole message.
			int count = cm.getPayloadSize() / writeBuffer.capacity();
			if(cm.getPayloadSize() % writeBuffer.capacity() > 0) {
				count++;
			}
			
			// Send the chunks 
			int offset = 0;
			int lenght = (cm.getPayloadSize() >= writeBuffer.capacity())? writeBuffer.capacity() : cm.getPayloadSize();
			for(int i = 0; i < count; i++) {
				writeBuffer.clear();
				writeBuffer.put(cm.getPayload(), offset, lenght);
				writeBuffer.flip();

				channelWrite(channel, writeBuffer);
				offset += writeBuffer.capacity();
				if(i == count-2) {
					lenght = cm.getPayloadSize() - offset;
				} else {
					lenght = writeBuffer.capacity();
				}
			}
			
		} catch (KPIConnectorException ex) {
			throw new KPIConnectorException("Cannot write the message in the socket channel" + ex.getMessage(), ex);
		}	
	}

	 /** 
     * Write the contents of a ByteBuffer to the given SocketChannel.
     * 
     * @param	channel		The SocketChannel object to be used when sending the contents from the buffer
     * @param	writeBuffer	The ByteBuffer object that contains the bytes to be sent to the user
     * @return	None
     * 
     */
    private void channelWrite(SocketChannel channel, ByteBuffer writeBuffer) throws KPIConnectorException {
		long nbytes = 0;
		long toWrite = writeBuffer.remaining();
	
		// loop on the channel.write() call since it will not necessarily
		// write all bytes in one shot
		try {
		    while (nbytes != toWrite) {
		    	nbytes += channel.write(writeBuffer);
		    }
		} catch (ClosedChannelException cce) {
			cce.printStackTrace();
			throw new KPIConnectorException("The socket was closed: " + cce.getMessage(), cce);
		} catch (Exception e) {
			e.printStackTrace();
			throw new KPIConnectorException("Cannot write data in the socket channel: " + e.getMessage(), e);
		} 
	
		// get ready for another write if needed
		writeBuffer.rewind();

    }

    /** 
     * Adds a new message in the output queue
     * 
     * @param	msg		An array of bytes containing the data to be sent
     * @return	None
     * 
     */
	@Override
	public void write(byte[] msg) {
		outQueue.add(new ClientMessage(msg));
		synchronized(lock) {
			lock.notifyAll();
		}
	}	
	
	/**
	 * Adds a incoming message to the incoming messages queue. 
	 * 
	 * @param 	cm 	The ClientMessage object that contains the data received from the SIB
	 * @return	None
	 */
	public void addIncomingMessage(ClientMessage cm) {
		inQueue.add(cm);
		synchronized(lock) {
			lock.notifyAll();
		}
	}
	
	
	/**
	 * Used to gracefully stop both writing and reading threads from the TcpIpConnector object 
	 * 
	 * @return	None
	 */
	public void destroy() {
		
		if (this.keepAlive){
			//set control flag
			running = false;
			
			if(reader != null) {
				reader.shutdown();
			}
			
			reader = null;
			thread = null;
		}
		//if keep alive is set to false, we should not stop the main thread, only the reading one
		else{
			if(reader != null) {
				reader.shutdown();
			}
			
			reader = null;
		}
		
		
	}
	
	/**
	 * Getters and setters
	 */
	public ConnectionSupport getConnectionSupport() {
		return connectionListeners;
	}
	@Override
	public String getName() {
		return "TCP/IP";
	}
	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		// Establish the keepAlive property.
		String tmp = this.properties.getProperty(TcpipConnector.KEEP_ALIVE); 
		if(tmp != null) {
			this.keepAlive = Boolean.parseBoolean(tmp);
		}
	}


	public boolean isRunning() {
		return running;
	}
	
	
	
	
}
