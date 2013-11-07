package com.asksunny.protocol.rpc;

import java.io.IOException;



public interface ProtocolDecodeHandler 
{	
	
	public void onSocketIOError(IOException iex);
	public void onReceive(RPCEnvelope envelope);		
	
}
