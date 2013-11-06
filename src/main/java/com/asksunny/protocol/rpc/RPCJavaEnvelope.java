package com.asksunny.protocol.rpc;

import com.asksunny.cli.utils.CLICommand;

public class RPCJavaEnvelope extends AbstractRPCEnvelope 
{
	
	Object jarSource;
	String className;
	
	
	public RPCJavaEnvelope()
	{
		super.envelopeType = (RPC_ENVELOPE_TYPE_JAVA);
		super.rpcType = RPC_TYPE_REQUEST;
	}
	
	
	
	
	
	public static RPCJavaEnvelope createJavaEnvelope(CLICommand rcmd)
	{
		RPCJavaEnvelope envelope = new RPCJavaEnvelope();
		envelope.setRpcType(RPC_TYPE_REQUEST);	
		rcmd.shift();
		envelope.setJarSource(rcmd.shift());
		envelope.setClassName(rcmd.shift());
		for(String cmd: rcmd.getCmdArray()){			
			envelope.addRpcObjects(cmd);
		}		
		return envelope;
		
	}





	public Object getJarSource() {
		return jarSource;
	}





	public RPCJavaEnvelope setJarSource(Object jarSource) {
		this.jarSource = jarSource;
		return this;
	}





	public String getClassName() {
		return className;
	}





	public RPCJavaEnvelope setClassName(String className) {
		this.className = className;
		return this;
	}
	
	
	
}
