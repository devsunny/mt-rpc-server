package com.asksunny.protocol.rpc;

public enum RPCAdminCommand {

	PING(1), ECHO(2), UPTIME(3), STATUS(4), HEARTBEAT(5), SHUTDOWN(9999);

	private final int cmd;

	RPCAdminCommand(int cmd) {
		this.cmd = cmd;
	}

	public int getValue() {
		return this.cmd;
	}

	public static RPCAdminCommand valueOf(int val) {
		switch (val) {
		case 1:
			return PING;
		case 2:
			return ECHO;
		case 3:
			return UPTIME;
		case 4:
			return STATUS;
		case 5:
			return HEARTBEAT;
		case 9999:
			return SHUTDOWN;
		default:
			return PING;
		}
	}
	
	

}
