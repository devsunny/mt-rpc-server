package com.asksunny.protocol.rpc;

public class RPCAdminEnvelope extends AbstractRPCEnvelope {

		
	int adminCommand;
	
	public RPCAdminEnvelope() {
		super.envelopeType = RPC_ENVELOPE_TYPE_ADMIN;
		super.rpcType = RPC_TYPE_REQUEST;
	}

	public int getAdminCommand() {
		return adminCommand;
	}

	public RPCAdminEnvelope setAdminCommand(RPCAdminCommand adminCommand) {
		this.adminCommand = adminCommand.getValue();
		return this;
	}

	
	
	
}
