package com.asksunny.rpc.admin;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.protocol.rpc.RPCAdminCommand;
import com.asksunny.protocol.rpc.RPCAdminEnvelope;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.rpc.mtserver.RPCRuntime;

public class AdminRPCRuntime  implements RPCRuntime{

	ExecutorService executorService;
	final static Logger log = LoggerFactory.getLogger(AdminRPCRuntime.class);
	
	
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
			response.setAdminCommand(RPCAdminCommand.PING);
			response.addRpcObjects(System.currentTimeMillis());
		}else if(cmd == RPCAdminCommand.ECHO ){
			response.setAdminCommand(RPCAdminCommand.ECHO);
			response.setRpcObjects(request.getRpcObjects());
		}else if(cmd == RPCAdminCommand.UPTIME ){
			response.setAdminCommand(RPCAdminCommand.UPTIME);
			response.addRpcObjects(System.currentTimeMillis());
		}else if(cmd == RPCAdminCommand.STATUS ){
			response.setAdminCommand(RPCAdminCommand.STATUS);
			response.addRpcObjects(System.currentTimeMillis());
		}else if(cmd == RPCAdminCommand.HEARTBEAT ){
			response.setAdminCommand(RPCAdminCommand.HEARTBEAT);
			response.addRpcObjects(System.currentTimeMillis());
		}else if(cmd == RPCAdminCommand.SHUTDOWN ){
			System.exit(0);
		}		
		return response;
	}

	
	public boolean accept(RPCEnvelope envelope) throws Exception {		
		return envelope.getRpcType()==RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN;
	}

}
