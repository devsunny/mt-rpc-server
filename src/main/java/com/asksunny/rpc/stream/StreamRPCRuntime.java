package com.asksunny.rpc.stream;

import java.util.concurrent.ExecutorService;

import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCStreamEnvelope;
import com.asksunny.rpc.mtserver.RPCRuntime;

public class StreamRPCRuntime implements RPCRuntime {

	ExecutorService executorService;

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public RPCEnvelope invoke(RPCEnvelope request) throws Exception {

		RPCStreamEnvelope response = new RPCStreamEnvelope();
		response.setRpcType(RPCEnvelope.RPC_TYPE_RESPONSE);

		return response;
	}

	public boolean accept(RPCEnvelope envelope) throws Exception {
		return envelope.getRpcType() == RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM;
	}

}
