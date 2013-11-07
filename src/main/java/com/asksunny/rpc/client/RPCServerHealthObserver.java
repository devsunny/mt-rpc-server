package com.asksunny.rpc.client;

public interface RPCServerHealthObserver {
	public void onServerResponseTimeout();
	public void onServerGone();
}
