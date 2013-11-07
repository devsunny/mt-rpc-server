package com.asksunny.rpc.admin;

import java.util.concurrent.ExecutorService;

import com.asksunny.protocol.rpc.RPCAdminCommand;
import com.asksunny.protocol.rpc.RPCAdminEnvelope;
import com.asksunny.protocol.rpc.RPCEnvelope;
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
		RPCAdminEnvelope adminRequest = (RPCAdminEnvelope)request;
		
		RPCAdminEnvelope response = new RPCAdminEnvelope();
		response.setRpcType(RPCEnvelope.RPC_TYPE_RESPONSE);
		RPCAdminCommand cmd = RPCAdminCommand.valueOf(adminRequest.getAdminCommand());
		if(cmd == RPCAdminCommand.PING ){
			
		}else if(cmd == RPCAdminCommand.ECHO ){
			
		}else if(cmd == RPCAdminCommand.UPTIME ){
			
		}else if(cmd == RPCAdminCommand.STATUS ){
			
		}else if(cmd == RPCAdminCommand.HEARTBEAT ){
			
		}else if(cmd == RPCAdminCommand.SHUTDOWN ){
			
		}		
		return response;
	}

	
	public boolean accept(RPCEnvelope envelope) throws Exception {		
		return envelope.getRpcType()==RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN;
	}

}
