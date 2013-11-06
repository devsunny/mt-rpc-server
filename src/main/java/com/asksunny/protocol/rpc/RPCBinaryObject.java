package com.asksunny.protocol.rpc;

public class RPCBinaryObject extends RPCObject
{
	String filename;
		

	public RPCBinaryObject()
	{
		super.objectType = RPCEnvelope.RPC_OBJECT_TYPE_BINARY;
	}
	
	public String getName() {
		return filename;
	}

	public RPCBinaryObject setName(String filename) {
		this.filename = filename;
		return this;
	}

		
	
}
