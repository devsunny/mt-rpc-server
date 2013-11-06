package com.asksunny.rpc.mtserver;

import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.rpc.admin.AdminRPCRuntime;
import com.asksunny.rpc.javart.JavaRPCRuntime;
import com.asksunny.rpc.shellrt.ShellRPCRuntime;
import com.asksunny.rpc.stream.StreamRPCRuntime;

public final class RPCRuntimeFactory {
	final static RPCRuntimeFactory defaultFactory = new RPCRuntimeFactory();

	
	
	RPCRuntime getRPCRuntime(RPCEnvelope env)
	{				
		switch(env.getEnvelopeType())
		{
		case RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN:
			return new AdminRPCRuntime();
		case RPCEnvelope.RPC_ENVELOPE_TYPE_SHELL:
			return new ShellRPCRuntime();
		case RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM:
			return new StreamRPCRuntime();
		case RPCEnvelope.RPC_ENVELOPE_TYPE_JAVA:
			return new JavaRPCRuntime();	
		default:
			return new ShellRPCRuntime();		
		}		
	}
	
	
	public static RPCRuntimeFactory getDefaultfactory() {
		return defaultFactory;
	}
	
	
	
	
}
