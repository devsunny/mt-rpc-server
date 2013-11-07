package com.asksunny.protocol.rpc;

import com.asksunny.cli.utils.CLICommand;

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

	public static RPCAdminEnvelope createAdminEnvelope(CLICommand remoteCommand) {

		String cmd = remoteCommand.shift();

		RPCAdminCommand command = null;

		command = RPCAdminCommand.valueOf(cmd.toUpperCase());
		RPCAdminEnvelope env = new RPCAdminEnvelope();
		env.setAdminCommand(command);
		for (String cmds : remoteCommand.getCmdArray()) {
			env.addRpcObjects(cmds);
		}

		return env;
	}

}
