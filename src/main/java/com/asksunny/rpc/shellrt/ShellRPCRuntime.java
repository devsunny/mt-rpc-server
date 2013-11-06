package com.asksunny.rpc.shellrt;

import static com.asksunny.protocol.rpc.RPCEnvelope.*;

import java.io.ByteArrayOutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.io.utils.StreamCopier;
import com.asksunny.protocol.rpc.RPCBinaryObject;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCObject;
import com.asksunny.protocol.rpc.RPCShellEnvelope;
import com.asksunny.rpc.mtserver.RPCRuntime;

public class ShellRPCRuntime implements RPCRuntime {

	final static Logger log = LoggerFactory.getLogger(ShellRPCRuntime.class);

	ExecutorService executorService;

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public boolean accept(RPCEnvelope envelope) throws Exception {
		return envelope.getEnvelopeType() == RPC_ENVELOPE_TYPE_SHELL;
	}

	public RPCEnvelope invoke(RPCEnvelope request) throws Exception {
		if (log.isInfoEnabled())
			log.info("RPC Request type {}", request.getRpcType());
		List<RPCObject> params = request.getRpcObjects();
		RPCShellEnvelope response = new RPCShellEnvelope();
		response.setRpcType(RPC_TYPE_RESPONSE);
		List<RPCObject> result = new ArrayList<RPCObject>();
		List<String> shellCommands = getShellCommands(params);
		if (OSUtil.isWindow()) {
			shellCommands.add(0, "cmd.exe");
			shellCommands.add(1, "/C");
		}
		ProcessBuilder pb = new ProcessBuilder(shellCommands);
		pb.redirectError(Redirect.PIPE);
		pb.redirectOutput(Redirect.PIPE);
		pb.redirectInput(Redirect.PIPE);
		Process p = pb.start();
		ByteArrayOutputStream err = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		StreamCopier.copy(p.getInputStream(), out);
		StreamCopier.copy(p.getErrorStream(), err);
		result.add(RPCObject.newInstance(RPC_OBJECT_TYPE_INT).setValue(
				p.exitValue()));
		result.add(new RPCBinaryObject().setName("STDOUT").setValue(
				out.toByteArray()));
		result.add(new RPCBinaryObject().setName("STDERR").setValue(
				err.toByteArray()));
		response.setRpcObjects(result);
		return response;
	}

	@SuppressWarnings("unchecked")
	protected List<String> getShellCommands(List<RPCObject> params) {
		List<String> shellCommands = new ArrayList<String>();

		for (RPCObject obj : params) {
			if (obj.getValue() == null)
				continue;
			StringBuilder buf = new StringBuilder();
			switch (obj.getObjectType()) {
			case RPC_OBJECT_TYPE_COLLECTION_INT:
				int[] ints = (int[]) obj.getValue();
				buf.setLength(0);
				for (int i = 0; i < ints.length; i++) {
					buf.append(ints[i]);
					if (i < ints.length - 1)
						buf.append(",");
				}
				shellCommands.add(buf.toString());
				break;
			case RPC_OBJECT_TYPE_COLLECTION_BOOLEAN:
				boolean[] bools = (boolean[]) obj.getValue();
				buf.setLength(0);
				for (int i = 0; i < bools.length; i++) {
					buf.append(bools[i]);
					if (i < bools.length - 1)
						buf.append(",");
				}
				shellCommands.add(buf.toString());
				break;
			case RPC_OBJECT_TYPE_COLLECTION_LONG:
				long[] longs = (long[]) obj.getValue();
				buf.setLength(0);
				for (int i = 0; i < longs.length; i++) {
					buf.append(longs[i]);
					if (i < longs.length - 1)
						buf.append(",");
				}
				shellCommands.add(buf.toString());
				break;
			case RPC_OBJECT_TYPE_COLLECTION_DOUBLE:
				double[] doubles = (double[]) obj.getValue();
				buf.setLength(0);
				for (int i = 0; i < doubles.length; i++) {
					buf.append(doubles[i]);
					if (i < doubles.length - 1)
						buf.append(",");
				}
				shellCommands.add(buf.toString());
				break;
			case RPC_OBJECT_TYPE_COLLECTION_STRING:
				String[] strs = (String[]) obj.getValue();
				buf.setLength(0);
				for (int i = 0; i < strs.length; i++) {
					buf.append(strs[i]);
					if (i < strs.length - 1)
						buf.append(",");
				}
				shellCommands.add(buf.toString());
				break;
			case RPC_OBJECT_TYPE_MAP_STRING:
				Map<String, String> map = (Map<String, String>) obj.getValue();
				for (String s1 : map.keySet()) {
					String valstr = map.get(s1);
					shellCommands.add("-" + s1);
					shellCommands.add(valstr);
				}
			default:
				shellCommands.add(obj.getValue().toString());
			}

		}

		return shellCommands;
	}

}
