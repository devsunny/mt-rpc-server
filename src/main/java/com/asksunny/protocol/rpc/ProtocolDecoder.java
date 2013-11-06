package com.asksunny.protocol.rpc;

import java.io.IOException;
import java.io.InputStream;


public interface ProtocolDecoder {	
	
	public void register(ProtocolDecodeHandler decodeHandler);
	
	public long decode(InputStream in) throws IOException;
	
	public RPCEnvelope decodeNow(InputStream in) throws IOException;
	
	public long decodeNow(InputStream in, ProtocolDecodeHandler decodeHandler) throws IOException;
}
