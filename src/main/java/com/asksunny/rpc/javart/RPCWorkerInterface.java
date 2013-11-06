package com.asksunny.rpc.javart;

import java.util.List;

import com.asksunny.protocol.rpc.RPCObject;

public interface RPCWorkerInterface {
	List<RPCObject> execute(List<RPCObject> args) throws Exception;
}
