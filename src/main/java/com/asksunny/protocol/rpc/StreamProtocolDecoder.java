package com.asksunny.protocol.rpc;

import static com.asksunny.protocol.rpc.RPCEnvelope.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.io.utils.StreamCopier;

public class StreamProtocolDecoder implements ProtocolDecoder {

	final static Logger log = LoggerFactory.getLogger(StreamProtocolDecoder.class);
	List<ProtocolDecodeHandler> registeredHandlers = new ArrayList<ProtocolDecodeHandler>();
	
	public void register(ProtocolDecodeHandler decodeHandler) {
		registeredHandlers.add(decodeHandler);
	}

	public long decode(InputStream in) throws IOException 
	{
		return decodeNow(in, registeredHandlers);
	}
	
	
	protected short readShort(InputStream objIn)  throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(2);
		objIn.read(buf.array());
		return buf.getShort();
	}
	protected int readInt(InputStream objIn)  throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(4);
		objIn.read(buf.array());
		return buf.getInt();
	}
	
	protected long readLong(InputStream objIn)  throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(8);
		objIn.read(buf.array());
		return buf.getLong();
	}
	
	protected double readDouble(InputStream objIn)  throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(8);
		objIn.read(buf.array());
		return buf.getDouble();
	}
	
	protected void readFully(InputStream objIn, byte[] b)  throws IOException
	{
		for(int i=0; i<b.length; i++){
			b[i] = (byte)objIn.read();
		}
	}

	
	public RPCEnvelope decodeNow(InputStream objIn) throws IOException {
		
		long bytesReceived = 0L;		
		short  envelopeType = -1;
		try{
			envelopeType = readShort(objIn);
			bytesReceived =+ 2L;
		}catch(IOException ex){			
			try {
				objIn.close();
			} catch (Exception e) {
				;
			}
			return null;
		}
		
		RPCEnvelope envelope  = null;
		switch(envelopeType)
		{
		case RPC_ENVELOPE_TYPE_MESSAGE:
			envelope  = decodeMessage(objIn);
			break;
		case RPC_ENVELOPE_TYPE_SHELL:
			envelope  = decodeShellEnvelope(objIn);
			break;
		case RPC_ENVELOPE_TYPE_STREAM:
			envelope  = decodeStream(objIn);
			break;
		case RPC_ENVELOPE_TYPE_JAVA:
			envelope  = decodeJavaEnvelope(objIn);
			break;
		case RPC_ENVELOPE_TYPE_ADMIN:
			envelope  = decodeAdminEnvelope(objIn);
			break;		
		case RPC_END_SESSION:
			envelope  = null;
			try {
				objIn.close();
			} catch (Exception e) {
				;
			}
			break;
		default:
			throw new IOException(String.format("Unexpected RPC envelope type. [%d]", envelopeType));
		}
		
		if(envelope!=null) ((AbstractRPCEnvelope)envelope).addReceivedInBytes(bytesReceived);
		return envelope;
	}

	public long decodeNow(InputStream in, ProtocolDecodeHandler decodeHandler)
			throws IOException {
		return decodeNow(in, Arrays.asList(decodeHandler));
	}
	
	
	protected long decodeNow(InputStream in, List<ProtocolDecodeHandler> decodeHandlers)
			throws IOException
	{
		
		long bytesReceived = 0L;
		RPCEnvelope envelope = null;		
		while((envelope = decodeNow(in))!=null){
			for (ProtocolDecodeHandler protocolDecodeHandler : decodeHandlers) {
				bytesReceived += ((AbstractRPCEnvelope)envelope).getReceivedInBytes();
				protocolDecodeHandler.onReceive(envelope);				
			}			
		}
		return bytesReceived;
	}
	
	
	protected  RPCShellEnvelope decodeShellEnvelope(InputStream objIn) throws IOException
	{
		long bytesReceived = 0L;
		RPCShellEnvelope envelope = new RPCShellEnvelope();
		long envelopeId = readLong(objIn);	
		bytesReceived += 8L;
		short rpcType =readShort( objIn);	
		bytesReceived += 2L;
		envelope.setEnvelopeId(envelopeId);
		envelope.setRpcType(rpcType);
		List<RPCObject> objs =  decodeRPCObjects(objIn);
		if(objs!=null){
			for (RPCObject rpcObject : objs) {
				bytesReceived += rpcObject.getReceivedInBytes();
			}
		}
		envelope.setRpcObjects(objs);
		envelope.setReceivedInBytes(bytesReceived);
		return envelope;
	}
	
	
	
	protected  List<RPCObject> decodeRPCObjects(InputStream objIn) throws IOException
	{
		
		int args_len = readInt(objIn);
		if(args_len==-1) return null;
		List<RPCObject> list = new ArrayList<RPCObject>();
		for (int i = 0; i < args_len; i++) {
			list.add(decodeRPCObject(objIn));			
		}		
		return list;
	}
	
	protected  RPCObject decodeRPCObject(InputStream objIn) throws IOException
	{
		long bytesReceived = 0L;
		short objType =readShort( objIn);
		RPCObject obj =  RPCObject.newInstance(objType);
		
		bytesReceived += 2L;
		Object value = null;
		int el_len = 0;
		int i = 0;
		switch(objType)
		{
		case RPC_OBJECT_TYPE_BOOLEAN:
			int bi = objIn.read();
			bytesReceived += 1L;
			value = bi<=0? Boolean.FALSE:Boolean.TRUE;	
			break;
		case RPC_OBJECT_TYPE_INT:
			value = new Integer(readInt(objIn));	
			bytesReceived += 4L;
			break;
		case RPC_OBJECT_TYPE_LONG:
			value = new Long(readLong(objIn));
			bytesReceived += 8L;
			break;
		case RPC_OBJECT_TYPE_DOUBLE:
			value = new Double(readDouble(objIn));
			bytesReceived += 8L;
			break;
		case RPC_OBJECT_TYPE_STRING:
			int str_len = readInt(objIn);
			bytesReceived += 4L;
			if(str_len==-1){
				value = null;
			}else{
				byte[] str_buf = new byte[str_len];
				readFully(objIn, str_buf);
				bytesReceived += str_len;
				value = new String(str_buf);
			}
			break;
		case RPC_OBJECT_TYPE_BINARY:
			int fn_len = readInt(objIn);
			bytesReceived += 4L;
			byte[] fn_buf = new byte[fn_len];
			readFully(objIn, fn_buf);
			bytesReceived += fn_len;
			((RPCBinaryObject)obj).setName(new String(fn_buf));
			long bin_len = readLong(objIn);
			bytesReceived += 8L;
			if(bin_len==-1){
				value = null;
			}else{
				byte[] bin_buf = new byte[(int)bin_len];
				readFully(objIn, bin_buf);
				bytesReceived += bin_len;
				value = bin_buf;
			}
			break;
		case RPC_OBJECT_TYPE_COLLECTION_BOOLEAN:
			el_len = readInt(objIn);
			bytesReceived += 4L;
			if(el_len==-1){
				value = null;
			}else{
				boolean[] bs = new boolean[el_len];
				for(i=0; i<el_len; i++){
					int bi1 = objIn.read();
					bytesReceived += 1L;
					bs[i] = bi1<=0?false:true;
				}
				value = bs;
			}
			break;
		case RPC_OBJECT_TYPE_COLLECTION_INT:
			el_len = readInt(objIn);
			bytesReceived += 4L;
			if(el_len==-1){
				value = null;
			}else{
				int[] ints = new int[el_len];
				for(i=0; i<el_len; i++){					
					ints[i] = readInt(objIn);
					bytesReceived += 4L;
				}
				value = ints;
			}			
			break;
		case RPC_OBJECT_TYPE_COLLECTION_LONG:
			el_len = readInt(objIn);
			bytesReceived += 4L;
			if(el_len==-1){
				value = null;
			}else{
				long[] ain = new long[el_len];
				for(i=0; i<el_len; i++){					
					ain[i] = readLong(objIn);
					bytesReceived += 8L;
				}
				value = ain;
			}	
			break;
		case RPC_OBJECT_TYPE_COLLECTION_DOUBLE:			
			el_len = readInt(objIn);
			bytesReceived += 4L;
			if(el_len==-1){
				value = null;
			}else{
				double[] ain = new double[el_len];
				for(i=0; i<el_len; i++){					
					ain[i] = readDouble(objIn);
					bytesReceived += 8L;
				}
				value = ain;
			}	
			break;
		case RPC_OBJECT_TYPE_COLLECTION_STRING:
			el_len = readInt(objIn);
			bytesReceived += 4L;
			if(el_len==-1){
				value = null;
			}else{
				String[] ain = new String[el_len];
				for(i=0; i<el_len; i++){					
					int strlen = readInt(objIn);
					bytesReceived += 4L;
					if(strlen==-1){
						ain[i] = null;
					}else{
						byte[] strbuf = new byte[strlen];
						readFully(objIn, strbuf);
						bytesReceived += strlen;
						ain[i] = new String(strbuf);						
					}
				}
				value = ain;
			}	
			break;
		case RPC_OBJECT_TYPE_MAP_STRING:
			el_len = readInt(objIn);
			bytesReceived += 4L;
			if(el_len==-1){
				value = null;
			}else{
				Map<String, String> map = new HashMap<String, String>();				
				for(i=0; i<el_len; i++){					
					int key_len = readInt(objIn);
					bytesReceived += 4L;
					byte[] keybuf = new byte[key_len];
					readFully(objIn, keybuf);
					bytesReceived += key_len;
					int val_len = readInt(objIn);
					bytesReceived += 4L;
					if(val_len>RPC_OBJECT_VAL_NIL){						
						byte[] valbuf = new byte[val_len];
						readFully(objIn, valbuf);
						bytesReceived += val_len;
						map.put(new String(keybuf), new String(valbuf));
					}
				}
				value = map;
			}	
			break;
		}		
		obj.setValue(value).setReceivedInBytes(bytesReceived);
		return obj;
	}
	
	protected  RPCAdminEnvelope decodeAdminEnvelope(InputStream objIn)  throws IOException
	{
		long bytesReceived = 0L;
		RPCAdminEnvelope envelope = new RPCAdminEnvelope();
		int val = readInt(objIn);
		bytesReceived += 4;
		envelope.setAdminCommand(RPCAdminCommand.valueOf(val));
		
		
		List<RPCObject> objs =  decodeRPCObjects(objIn);
		if(objs!=null){
			for (RPCObject rpcObject : objs) {
				bytesReceived += rpcObject.getReceivedInBytes();
			}
		}
		envelope.setRpcObjects(objs);
		envelope.setReceivedInBytes(bytesReceived);		
		return envelope;
	}
	
	protected  RPCJavaEnvelope decodeJavaEnvelope(InputStream objIn)  throws IOException
	{
		long bytesReceived = 0L;
		RPCJavaEnvelope envelope = new RPCJavaEnvelope();
		
		//decode destination here;
		int jar_len = readInt(objIn);
		bytesReceived +=4L;
		if(jar_len!=-1){
			byte[] jarContent = new byte[jar_len];
			readFully(objIn, jarContent);
			envelope.setJarSource(jarContent);
			bytesReceived += jar_len;
		}	
		int clz_len = readInt(objIn);
		bytesReceived +=4L;
		String clz_name = readString(objIn, clz_len);
		bytesReceived += clz_len;
		envelope.setClassName(clz_name);
		List<RPCObject> objs =  decodeRPCObjects(objIn);
		if(objs!=null){
			for (RPCObject rpcObject : objs) {
				bytesReceived += rpcObject.getReceivedInBytes();
			}
		}
		envelope.setRpcObjects(objs);
		envelope.setReceivedInBytes(bytesReceived);		
		return envelope;
	}
	
	
	protected  RPCStreamEnvelope decodeStream(InputStream objIn)  throws IOException
	{
		long bytesReceived = 0L;
		RPCStreamEnvelope envelope = new RPCStreamEnvelope();
		if(log.isDebugEnabled()) log.debug("Decode Stream");
		short rpcType =readShort( objIn);
		envelope.setRpcType(rpcType);
		if(log.isDebugEnabled()) log.debug("rpcType {}", ((rpcType==RPC_TYPE_REQUEST)?"Request":"Response") );
		
		int src_len = readInt(objIn);
		bytesReceived +=4L;
		if(src_len!=-1){
			envelope.setSource(readString(objIn, src_len));
			bytesReceived += src_len;
			if(log.isDebugEnabled()) log.debug("Source {}", envelope.getSource() );
		}	
		
		//decode destination here;
		int dest_len = readInt(objIn);
		bytesReceived +=4L;
		if(dest_len!=-1){
			envelope.setDestination(readString(objIn, dest_len));
			bytesReceived += dest_len;
			if(log.isDebugEnabled()) log.debug("getDestination {}", envelope.getDestination());
		}		
		long length = readLong(objIn);
		if(log.isDebugEnabled()) log.debug("Stream length [{}]", length);
		bytesReceived +=8L;
		if(length>0){			
			if( envelope.getDestination()!=null){
				FileOutputStream fout = null;
				try{
					fout = new FileOutputStream( envelope.getDestination());
					bytesReceived += StreamCopier.copy(objIn, 0, length,  fout);
					fout.flush();
				}finally{
					if(fout!=null) fout.close();
				}
			}else{
				bytesReceived += length;
				objIn.skip(length);
			}
			envelope.setLength(length).setReceivedInBytes(bytesReceived);
			
		}else{
			
			envelope.setLength(-1).setReceivedInBytes(bytesReceived);
		}
		if(log.isDebugEnabled()) log.debug("Stream decode complete, total bytes received [{}]", bytesReceived);
		return envelope;
	}
	
	protected  RPCMessageEnvelope  decodeMessage(InputStream objIn)  throws IOException
	{
		long bytesReceived = 0L;
		RPCMessageEnvelope envelope = new RPCMessageEnvelope();
		int length = readInt(objIn);
		bytesReceived +=4L;
		byte[] buf = new byte[length];
		readFully(objIn, buf);
		bytesReceived += length;
		envelope.setMessage(buf).setReceivedInBytes(bytesReceived);
		return envelope;
	}
	
	protected String readString(InputStream objIn, int length) throws IOException
	{
		byte[] buf = new byte[length];
		readFully(objIn, buf);
		return new String(buf);
	}

}
