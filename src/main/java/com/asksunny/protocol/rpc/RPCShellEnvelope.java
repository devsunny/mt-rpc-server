package com.asksunny.protocol.rpc;

import com.asksunny.cli.utils.CLICommand;

public class RPCShellEnvelope extends AbstractRPCEnvelope 
{
	
	
	public RPCShellEnvelope()
	{
		super.envelopeType = (RPC_ENVELOPE_TYPE_SHELL);
		super.rpcType = RPC_TYPE_REQUEST;
	}
	
	
	public static RPCShellEnvelope createShellCommand(CLICommand rcmd)
	{
		RPCShellEnvelope envelope = new RPCShellEnvelope();
		envelope.setRpcType(RPC_TYPE_REQUEST);		
		for(String cmd: rcmd.getCmdArray()){			
			envelope.addRpcObjects(cmd);
		}		
		return envelope;
		
	}
	
}
