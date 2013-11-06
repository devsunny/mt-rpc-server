package com.asksunny.protocol.rpc;

import java.io.InputStream;


/**
 * this is super fast transport type; there is nearly zero over head
 * @author SunnyLiu
 *
 */
public class RPCStreamEnvelope extends AbstractRPCEnvelope 
{
	
	InputStream stream;
	long length;
	String destination;
	String source;
	
	
	public RPCStreamEnvelope()
	{
		super.envelopeType = (RPC_ENVELOPE_TYPE_STREAM);
		super.rpcType = RPC_TYPE_REQUEST;
	}
	
	

	public String getSource() {
		return source;
	}



	public void setSource(String source) {
		this.source = source;
	}



	public InputStream getStream() {
		return stream;
	}

	public RPCStreamEnvelope setStream(InputStream stream) {
		this.stream = stream;
		return this;
	}

	public long getLength() {
		return length;
	}

	public RPCStreamEnvelope setLength(long length) {
		this.length = length;
		return this;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
	
		
}
