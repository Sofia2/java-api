package com.indra.sofia2.ssap.testutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

public class LightHttpListener implements Callable<String> {
	private int port = 3003;
	
	public LightHttpListener(int port) {
		this.port = port;
	}
	public String call() throws Exception {
		String retStr = null;
		try (ServerSocket server = new ServerSocket(port)) {
			server.setSoTimeout(0);

			//Waiting for client (sofia2) connection
			Socket client = server.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			
			//Reading http header
			String s;
			int contentLength = -1;
            while (!(s = in.readLine()).isEmpty()) {
                String [] header = s.split(":");
                if(header[0].equalsIgnoreCase("Content-Length")) {
                	contentLength = Integer.valueOf(header[1].trim());
                }
            }
            
            //Reading http content
            char [] buffer = new char[contentLength];
            in.read(buffer, 0, contentLength);

            //Sending response to client
           	out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Length: 0\r\n");
            out.write("Connection: close\r\n");
            out.write("\r\n");
            out.flush();
            retStr = String.valueOf(buffer);
            client.close();
	  	} catch (IOException e) {
			e.printStackTrace();
		}
		
		return retStr;
		
	}

	
	

}
