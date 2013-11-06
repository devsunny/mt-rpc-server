package com.asksunny.protocol.rpc;

import java.util.List;


public interface RPCEnvelope 
{
	
	
	public static final short RPC_OBJECT_VAL_NIL = -1;	
	public static final short RPC_OBJECT_TYPE_INT = 100;
	public static final short RPC_OBJECT_TYPE_BOOLEAN = 101;
	public static final short RPC_OBJECT_TYPE_LONG = 110;
	public static final short RPC_OBJECT_TYPE_DOUBLE = 120;
	public static final short RPC_OBJECT_TYPE_STRING = 130;	
	public static final short RPC_OBJECT_TYPE_BINARY = 140;
	
	public static final short RPC_OBJECT_TYPE_COLLECTION_INT = 201;
	public static final short RPC_OBJECT_TYPE_COLLECTION_BOOLEAN = 202;
	public static final short RPC_OBJECT_TYPE_COLLECTION_LONG = 211;
	public static final short RPC_OBJECT_TYPE_COLLECTION_DOUBLE = 221;
	public static final short RPC_OBJECT_TYPE_COLLECTION_STRING = 231;
	
	public static final short RPC_OBJECT_TYPE_MAP_STRING = 301;
	
	
	public static final short RPC_TYPE_REQUEST = 1001;	
	public static final short RPC_TYPE_RESPONSE = 1101;	
	
	
	public static final short RPC_ADMIN_ECHO = 100;
	public static final short RPC_ADMIN_UPTIME = 101;
	public static final short RPC_ADMIN_SERVER_TIME_MILLI = 102;
	public static final short RPC_ADMIN_SERVER_TIME_NANO = 103;	
	public static final short RPC_ADMIN_SHUTDOWN = 999;
	
	public static final short RPC_RESPONSE_TYPE_ERROR_CODE = 9001;	
	public static final short RPC_RESPONSE_TYPE_ERROR_MSG = 9011;
		
	
	
	public static final short RPC_ENVELOPE_TYPE_SHELL = 1;	
	public static final short RPC_ENVELOPE_TYPE_MESSAGE = 7;
	public static final short RPC_ENVELOPE_TYPE_STREAM = 15;
	public static final short RPC_ENVELOPE_TYPE_ADMIN = 31;
	public static final short RPC_ENVELOPE_TYPE_JAVA = 3;
	
			
	public static final short RPC_END_SESSION = -1;
	
	public RPCEnvelope addRpcObjects(String message);
	public RPCEnvelope addRpcObjects(int message);
	public RPCEnvelope addRpcObjects(long message);
	public RPCEnvelope addRpcObjects(double message);
	public RPCEnvelope addRpcObjects(boolean message);
	public RPCEnvelope addRpcObjects(String[] message);
	public RPCEnvelope addRpcObjects(int[] message);
	public RPCEnvelope addRpcObjects(long[] message);
	public RPCEnvelope addRpcObjects(double[] message);
	public RPCEnvelope addRpcObjects(boolean[] message);	
	public RPCEnvelope addRpcObjects(short rpcObjectType, Object value);
	public RPCEnvelope addRpcObjects(RPCObject rpcObject);
	public short getRpcType();
	public short getEnvelopeType();
	public List<RPCObject> getRpcObjects();
	
	
}
