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

public class TCPIPConfiguration {

	/** client buffer size in bytes */
	public static final int BUFFER_SIZE = 32768;
	
	/** client buffer grow size in bytes */
	public static final int BUFFER_GROW = 100;
	
	/** port the server listens on */
	public static final String PORT = "PORT";

	/** size of ByteBuffer for reading/writing from channels */
	public static final String NET_BUFFER_SIZE = "NET_BUFFER_SIZE";

	/** interval to sleep between attempts to write to a channel. */
	public static final String CHANNEL_WRITE_SLEEP = "CHANNEL_WRITE_SLEEP";

	/** number of worker threads for EventWriter */
	public static final String EVENT_WRITER_WORKERS = "EVENT_WRITER_WORKERS";

	/** default number of workers for GameControllers */
	public static final String CONTROLLER_WORKERS = "DEFAULT_CONTROLLER_WORKERS";
	
	/** default size of the write queue */
	public static final String WRITE_QUEUE_SIZE = "WRITE_QUEUE_SIZE";
	
	/** default sleep for reader channels */
	public static final String CHANNEL_READER_SLEEP = "CHANNEL_READER_SLEEP";
	
	/** The TCP/IP Gateway parameter default values */
	public static final int DEFAULT_PORT = 23000;
	public static final int DEFAULT_NET_BUFFER_SIZE = 512;
	public static final long DEFAULT_CHANNEL_WRITE_SLEEP = 10;
	public static final int DEFAULT_EVENT_WRITER_WORKERS = 5;
	public static final int DEFAULT_CONTROLLER_WORKERS = 5;
	public static final int DEFAULT_WRITE_QUEUE_SIZE = 100;
	public static final long DEFAULT_CHANNEL_READER_SLEEP = 30;

}
