package com.asksunny.rpc.rtclient;

import java.util.ArrayList;
import java.util.List;

import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCObject;
import com.asksunny.rpc.javart.RPCWorkerInterface;

public class RPCWorkerJarTest implements RPCWorkerInterface {

	public List<RPCObject> execute(List<RPCObject> args) throws Exception {
		
		List<RPCObject> objs = new ArrayList<RPCObject>();		
		objs.add(RPCObject.newInstance(RPCEnvelope.RPC_OBJECT_TYPE_COLLECTION_STRING).setValue("This is returned from server execution2"));
		return objs;
	}

}
