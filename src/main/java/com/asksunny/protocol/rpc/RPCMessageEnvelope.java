package com.asksunny.protocol.rpc;


/**
 * this is super fast transport type; there is nearly zero over head
 * @author SunnyLiu
 *
 */
public class RPCMessageEnvelope extends AbstractRPCEnvelope 
{
	
	
	
	byte[] message;
	
	
	public RPCMessageEnvelope()
	{
		super.envelopeType = (RPC_ENVELOPE_TYPE_MESSAGE);
		super.setRpcType(RPC_TYPE_RESPONSE);
	}
	
	
	public RPCMessageEnvelope setMessage(byte[] message)
	{
		this.message = message;
		return this;
	}


	public byte[] getMessage() {
		return message==null?new byte[0]:message;
	}
	
	public int getMessageLength() {
		return message==null?0:message.length;
	}
	
}
