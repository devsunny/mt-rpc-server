package com.asksunny.protocol.rpc;

import java.io.IOException;
import java.io.OutputStream;

public interface ProtocolEncoder {
	public long encode(OutputStream out, RPCEnvelope command) throws IOException;
}
