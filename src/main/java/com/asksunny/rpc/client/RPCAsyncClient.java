package com.asksunny.rpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.protocol.rpc.ProtocolDecodeHandler;
import com.asksunny.protocol.rpc.RPCAdminCommand;
import com.asksunny.protocol.rpc.RPCAdminEnvelope;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.StreamProtocolDecoder;
import com.asksunny.protocol.rpc.StreamProtocolEncoder;

public class RPCAsyncClient implements Runnable {
	final static Logger log = LoggerFactory.getLogger(RPCAsyncClient.class);
	String remoteHost;
	int remotePort;
	boolean ssl = false;

	Socket clientSocket;
	InputStream sin = null;
	OutputStream sout = null;

	AtomicLong state = new AtomicLong(0L);
	AtomicBoolean stop = new AtomicBoolean(false);
	StreamProtocolDecoder decoder = new StreamProtocolDecoder();
	StreamProtocolEncoder encoder = new StreamProtocolEncoder();

	ConcurrentLinkedQueue<ProtocolDecodeHandler> registeredHandlers = new ConcurrentLinkedQueue<ProtocolDecodeHandler>();
	ConcurrentLinkedQueue<RPCEnvelope> requestQueue = new ConcurrentLinkedQueue<RPCEnvelope>();

	public void connect() throws IOException {
		if (state.compareAndSet(0, 1)) {
			this.clientSocket = connectToRemoteHost();
			sin = this.clientSocket.getInputStream();
			sout = this.clientSocket.getOutputStream();
		}
	}

	public void registerHandler(ProtocolDecodeHandler handler) {
		registeredHandlers.add(handler);
	}

	public void sendRequest(RPCEnvelope request) {
		requestQueue.add(request);
	}

	public void run() {
		while (!stop.get()) {
			RPCEnvelope req = requestQueue.poll();
			if (req != null) {
				try {
					encoder.encode(sout, req);
					if(req.getEnvelopeType()==RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN){
						RPCAdminEnvelope admin = (RPCAdminEnvelope)req;
						if(admin.getAdminCommand()==RPCAdminCommand.SHUTDOWN.getValue()){
							RPCAdminEnvelope preq = (new RPCAdminEnvelope()).setAdminCommand(RPCAdminCommand.PING);
							sendRequest(preq);
						}
					}
					RPCEnvelope env = decoder.decodeNow(sin);
					for (ProtocolDecodeHandler handler : registeredHandlers) {
						handler.onReceive(env);
					}
				} catch (IOException iex) {
					shutdown();
					for (ProtocolDecodeHandler handler : registeredHandlers) {
						handler.onSocketIOError(iex);
					}
					
				} catch (Exception ex) {
					log.warn("Unexpected err", ex);
				}
			} else {				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					;
				}				
			}
		}
	}

	public void shutdown() {
		stop.compareAndSet(false, true);
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
			SocketFactory socketfactory = SocketFactory
					.getDefault();
			Socket socket =  socketfactory.createSocket(
					this.remoteHost, this.remotePort);
			client = socket;
		}
		client.setSoTimeout(30 * 1000);
		client.setKeepAlive(true);
		return client;
	}

	public RPCAsyncClient(String remoteHost, int remotePort) {
		super();
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	public RPCAsyncClient(String remoteHost, int remotePort, boolean ssl) {
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
