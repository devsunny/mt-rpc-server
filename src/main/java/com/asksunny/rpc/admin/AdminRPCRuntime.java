package com.asksunny.rpc.admin;

import java.util.concurrent.ExecutorService;

import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCMessageEnvelope;
import com.asksunny.rpc.mtserver.RPCRuntime;

public class AdminRPCRuntime  implements RPCRuntime{

	ExecutorService executorService;
	
	
	
	public ExecutorService getExecutorService() {
		return executorService;
	}


	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}


	public RPCEnvelope invoke(RPCEnvelope request) throws Exception 
	{
		
		RPCMessageEnvelope response = new RPCMessageEnvelope();
		
			
		
		return response;
	}

	
	public boolean accept(RPCEnvelope envelope) throws Exception {		
		return envelope.getRpcType()==RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN;
	}

}
