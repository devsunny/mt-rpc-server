package com.asksunny.rpc.client;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RPCServerHealthMonitor extends RPCAsyncClient {

	ConcurrentLinkedQueue<RPCServerHealthObserver> registeredHandlers = new ConcurrentLinkedQueue<RPCServerHealthObserver>();
	
	

	public RPCServerHealthMonitor(String remoteHost, int remotePort, boolean ssl) {
		super(remoteHost, remotePort, ssl);		
	}



	public RPCServerHealthMonitor(String remoteHost, int remotePort) {
		super(remoteHost, remotePort);		
	}



	@Override
	public void run() {
		

	}

}
