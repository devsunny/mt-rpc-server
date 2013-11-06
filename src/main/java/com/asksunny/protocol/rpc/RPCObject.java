package com.asksunny.protocol.rpc;

public class RPCObject {

	Object value;
	short objectType = RPCEnvelope.RPC_OBJECT_TYPE_STRING;
	long  receivedInBytes = 0L;
	
	public static RPCObject newInstance(short objectType)
	{		
		RPCObject ret = (objectType==RPCEnvelope.RPC_OBJECT_TYPE_BINARY)? new RPCBinaryObject():new RPCObject(objectType);
		return ret;
	}
	
	public RPCObject() {
	}
	
	public RPCObject(short type) {
		this.objectType = type;
	}

	public Object getValue() {
		return value;
	}

	public RPCObject setValue(Object value) {
		this.value = value;
		return this;
	}

	public short getObjectType() {
		return objectType;
	}

	public RPCObject setObjectType(short objectType) {

		if (objectType == RPCEnvelope.RPC_OBJECT_TYPE_BINARY) {
			throw new IllegalArgumentException(
					"Please use RPCBinaryObject instead, RPCBinaryObject provides richier properties.");
		}
		this.objectType = objectType;
		return this;
	}

	long getReceivedInBytes() {
		return receivedInBytes;
	}

	RPCObject setReceivedInBytes(long receivedInBytes) {
		this.receivedInBytes = receivedInBytes;
		return this;
	}
	
	public String toString()
	{
		return (getValue()==null)?"":getValue().toString();
	}

}
