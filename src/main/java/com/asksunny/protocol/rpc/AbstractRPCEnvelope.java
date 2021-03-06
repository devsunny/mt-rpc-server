package com.asksunny.protocol.rpc;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author SunnyLiu
 * 
 */
public abstract class AbstractRPCEnvelope implements RPCEnvelope {

	short envelopeType;
	short rpcType;
	/**
	 * This is unique Id generated by requester, when server response back
	 * envelope, client could be able to identify which request.
	 */
	long envelopeId;
	List<RPCObject> rpcObjects;

	
	public static RPCEnvelope newInstance(short envelopeType)
	{
		RPCEnvelope envelope = null;
		switch(envelopeType)
		{
		case RPC_ENVELOPE_TYPE_MESSAGE:
			envelope = new RPCMessageEnvelope();
			break;
		case RPC_ENVELOPE_TYPE_SHELL:
			envelope = new RPCShellEnvelope();
			break;
		case RPC_ENVELOPE_TYPE_JAVA:
			envelope = new RPCJavaEnvelope();
			break;
		case RPC_ENVELOPE_TYPE_ADMIN:
			envelope = new RPCAdminEnvelope();
			break;
		case RPC_ENVELOPE_TYPE_STREAM:
			envelope = new RPCStreamEnvelope();
			break;
		default:
			envelope = new RPCShellEnvelope();
			break;
		}		
		return envelope ;
	}
	
	
	public short getEnvelopeType() {
		return envelopeType;
	}

	public long getEnvelopeId() {
		return envelopeId;
	}

	public void setEnvelopeId(long envelopeId) {
		this.envelopeId = envelopeId;
	}

	public List<RPCObject> getRpcObjects() {
		return rpcObjects;
	}

	public RPCEnvelope addRpcObjects(RPCObject rpcObject) {
		if (this.rpcObjects == null)
			this.rpcObjects = new ArrayList<RPCObject>();
		this.rpcObjects.add(rpcObject);		
		return this;
	}

	public RPCEnvelope setRpcObjects(List<RPCObject> rpcObjects) {
		this.rpcObjects = rpcObjects;
		return this;
	}

	public short getRpcType() {
		return rpcType;
	}

	public RPCEnvelope setRpcType(short rpcType) {
		this.rpcType = rpcType;
		return this;
	}

	public RPCEnvelope addRpcObjects(String message) {
		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_STRING)).setValue(message));
	}

	public RPCEnvelope addRpcObjects(int message) {
		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_INT))
				.setValue(message));
	}

	public RPCEnvelope addRpcObjects(long message) {
		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_LONG))
				.setValue(message));
	}

	public RPCEnvelope addRpcObjects(double message) {
		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_DOUBLE))
				.setValue(message));
	}

	public RPCEnvelope addRpcObjects(boolean message) {
		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_BOOLEAN))
				.setValue(message));
	}

	public RPCEnvelope addRpcObjects(String[] message) {

		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_COLLECTION_STRING))
				.setValue(message));

	}

	public RPCEnvelope addRpcObjects(int[] message) {

		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_COLLECTION_INT))
				.setValue(message));

	}

	public RPCEnvelope addRpcObjects(long[] message)
	{
		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_COLLECTION_LONG))
				.setValue(message));
	}

	public RPCEnvelope addRpcObjects(double[] message)
	{
		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_COLLECTION_DOUBLE))
				.setValue(message));
	}

	public RPCEnvelope addRpcObjects(boolean[] message)
	{
		return addRpcObjects((new RPCObject(RPC_OBJECT_TYPE_COLLECTION_BOOLEAN))
				.setValue(message));
	}

	public RPCEnvelope addRpcObjects(short rpcObjectType, Object value)
	{
		return addRpcObjects((new RPCObject(rpcObjectType))
				.setValue(value));
	}
	
	long  receivedInBytes = 0L;
	long getReceivedInBytes() {
		return receivedInBytes;
	}

	AbstractRPCEnvelope setReceivedInBytes(long receivedInBytes) {
		this.receivedInBytes = receivedInBytes;
		return this;
	}
	
	AbstractRPCEnvelope addReceivedInBytes(long receivedInBytes) {
		this.receivedInBytes += receivedInBytes;
		return this;
	}

	public AbstractRPCEnvelope mkRequest()
	{
		this.rpcType = RPC_TYPE_REQUEST;
		return this;
	}
	
	public AbstractRPCEnvelope mkResponse()
	{
		this.rpcType = RPC_TYPE_RESPONSE;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("AbstractRPCEnvelope [envelopeType=").append(envelopeType)
		.append(", rpcType=").append(rpcType).append(", envelopeId=").append(envelopeId).append(", rpcObjects=");		
		if(getRpcObjects()!=null){
			for(RPCObject obj: getRpcObjects()){
				buf.append(obj.toString()).append("|");
			}
		}		
		buf.append("]");		
		return buf.toString();
	}
	
	
	

}
