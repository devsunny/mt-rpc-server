package com.asksunny.rpc.mtserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.StreamProtocolDecoder;
import com.asksunny.protocol.rpc.StreamProtocolEncoder;

public class RpcWorker implements Runnable {

	final static Logger log = LoggerFactory.getLogger(MtRpcServer.class);
	MtRpcServer server;
	Socket client;
	
	
	public RpcWorker(MtRpcServer server, Socket client)
	{
		this.server = server;
		this.client = client;		
	}
	
	public void run() 
	{
		if(log.isDebugEnabled()) log.debug("RCP worker is serving {} request now", client.getInetAddress());
		ServerStateMonitor.registerNewClient(this.client);
		try{
			InputStream cin = this.client.getInputStream();
			OutputStream cout = this.client.getOutputStream();
			StreamProtocolDecoder decoder = new StreamProtocolDecoder();
			StreamProtocolEncoder encoder = new StreamProtocolEncoder();
			RPCEnvelope envelope = null;
			while ((envelope=decoder.decodeNow(cin))!=null) {
				try{
					RPCRuntime rt = RPCRuntimeFactory.getDefaultfactory().getRPCRuntime(envelope);				
					if(log.isDebugEnabled()) log.debug("found the match runtime {}", rt.getClass().getName());
					RPCEnvelope response = rt.invoke(envelope);
					if(response!=null){
						encoder.encode(cout, response);
					}
				}catch(Throwable ex){
					log.error(String.format("Failed to execute client %s request %s", this.client.getInetAddress(), envelope.toString()), ex);	
				}
			}			
		}catch(IOException ex){
			log.error(String.format("Failed to handle client %s request", this.client.getInetAddress()), ex);			
		}finally{
			ServerStateMonitor.unregisterClient(this.client);
			try {
				if(client!=null && !client.isClosed()){
					client.close();
				}
			} catch (IOException e) {
				;
			}
		}
		
	}
	
	

}
