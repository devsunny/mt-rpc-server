package com.asksunny.rpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.protocol.rpc.ProtocolDecodeHandler;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.StreamProtocolDecoder;
import com.asksunny.protocol.rpc.StreamProtocolEncoder;

public class RPCBatchClient {
	final static Logger log = LoggerFactory.getLogger(RPCBatchClient.class);
	String remoteHost;
	int remotePort;
	boolean ssl = false;

	Socket clientSocket;
	InputStream sin = null;
	OutputStream sout = null;

	AtomicLong state = new AtomicLong(0L);
	
	StreamProtocolDecoder decoder = new StreamProtocolDecoder();
	StreamProtocolEncoder encoder = new StreamProtocolEncoder();

	
	public void connect() throws IOException {
		if (state.compareAndSet(0, 1)) {
			this.clientSocket = connectToRemoteHost();
			sin = this.clientSocket.getInputStream();
			sout = this.clientSocket.getOutputStream();
		}
	}

	
	
	public RPCEnvelope sendRequest(RPCEnvelope req) throws IOException {
		RPCEnvelope env = null;
		try {
			encoder.encode(sout, req);
			env = decoder.decodeNow(sin);			
			return env;		
		} catch (IOException iex) {
			shutdown();
		} catch (Exception ex) {
			log.warn("Unexpected err", ex);
			env = null;
		}
		return env;
	}
	

	
	public void sendRequest(RPCEnvelope req, ProtocolDecodeHandler handler) throws IOException {
		try {
			encoder.encode(sout, req);
			RPCEnvelope env = decoder.decodeNow(sin);			
			handler.onReceive(env);			
		} catch (IOException iex) {
			shutdown();
		} catch (Exception ex) {
			log.warn("Unexpected err", ex);
		}
		
	}

	public void shutdown() {		
		if (sin != null) {
			try {
				sin.close();
			} catch (Exception ex) {
				;
			}
			sin = null;
		}
		if (sout != null) {
			try {
				sout.close();
			} catch (Exception ex) {
				;
			}
			sout = null;
		}

		if (clientSocket != null) {
			try {
				clientSocket.close();
			} catch (Exception ex) {
				;
			}
			clientSocket = null;
		}
	}

	protected Socket connectToRemoteHost() throws IOException {
		Socket client = null;
		if (ssl) {
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(
					this.remoteHost, this.remotePort);
			client = sslsocket;
		} else {
			SocketFactory socketfactory = (SocketFactory) SocketFactory
					.getDefault();
			Socket sslsocket = (SSLSocket) socketfactory.createSocket(
					this.remoteHost, this.remotePort);
			client = sslsocket;
		}
		client.setSoTimeout(30 * 1000);
		client.setKeepAlive(true);
		return client;
	}

	public RPCBatchClient(String remoteHost, int remotePort) {
		super();
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	public RPCBatchClient(String remoteHost, int remotePort, boolean ssl) {
		super();
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.ssl = ssl;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

}
