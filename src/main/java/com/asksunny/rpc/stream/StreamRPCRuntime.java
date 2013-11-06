package com.asksunny.rpc.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;

import com.asksunny.io.utils.StreamCopier;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCShellEnvelope;
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

	public RPCEnvelope invoke(RPCEnvelope req) throws Exception {

		RPCStreamEnvelope request = (RPCStreamEnvelope)req;
		if(request.getRpcType()==RPCEnvelope.RPC_TYPE_REQUEST){			
			File f = new File(request.getSource());
			if(f.exists() && f.isFile() && f.canRead() )
			{
				RPCStreamEnvelope response = new RPCStreamEnvelope();
				response.setRpcType(RPCEnvelope.RPC_TYPE_RESPONSE);
				response.setDestination(request.getDestination());
				response.setSource(request.getSource());
				response.setLength(f.length());
				response.setStream(new FileInputStream(f));				
				return response;				
			}else{
				RPCShellEnvelope response = new RPCShellEnvelope();
				response.addRpcObjects(1).addRpcObjects("Remote file not found:" + request.getSource());
				return response;
			}
			
		}else{
			RPCShellEnvelope response = new RPCShellEnvelope();
			File f = new File(request.getDestination());
			if(!f.getParentFile().exists()){
				boolean mkdir = f.mkdirs();
				if(mkdir==false){
					response.addRpcObjects(1).addRpcObjects("Failed to create directory");					
				}else{
					FileOutputStream fout = new FileOutputStream(f);
					try{
						StreamCopier.copy(request.getStream(), fout);
						response.addRpcObjects(0).addRpcObjects(request.getLength()).addRpcObjects("Content saved to " + request.getDestination());	
					}finally{
						if(fout!=null) fout.close();
					}					
				}
			}	
			return response;
			
		}
		
	}

	public boolean accept(RPCEnvelope envelope) throws Exception {
		return envelope.getRpcType() == RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM;
	}

}
