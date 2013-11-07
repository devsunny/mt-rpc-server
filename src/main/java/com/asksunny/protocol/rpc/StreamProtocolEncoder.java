package com.asksunny.protocol.rpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.io.utils.StreamCopier;

import static com.asksunny.protocol.rpc.RPCEnvelope.*;

public class StreamProtocolEncoder implements ProtocolEncoder {

	final static Logger log = LoggerFactory.getLogger(StreamProtocolEncoder.class);
	public static final int MAX_BINARY_BUFFER_SIZE = 1024 * 1024; //1MB;
	public static final byte[]  BINARY_BUFFER = new byte[MAX_BINARY_BUFFER_SIZE];
	
	public long encode(OutputStream out, RPCEnvelope envelope)
			throws IOException {
		long byteSent = 0L;
		ObjectOutputStream objOut = null;
		if (out instanceof ObjectOutputStream) {
			objOut = (ObjectOutputStream) out;
		} else {
			objOut = new ObjectOutputStream(out);
		}		
		switch(envelope.getEnvelopeType())
		{
		case RPC_ENVELOPE_TYPE_MESSAGE:
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_MESSAGE");
			byteSent += encodeMessage(objOut, (RPCMessageEnvelope) envelope);
			break;
		case RPC_ENVELOPE_TYPE_SHELL:
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_SHELL");
			byteSent += encodeShellEnvelope(objOut, (RPCShellEnvelope) envelope);
			break;		
		case RPC_ENVELOPE_TYPE_STREAM:
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_STREAM");
			byteSent += encodeStream(objOut, (RPCStreamEnvelope) envelope);
			break;
		case RPC_ENVELOPE_TYPE_JAVA:	
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_JAVA");
			byteSent += encodeJavaEnvelope(objOut, (RPCJavaEnvelope) envelope);
			break;
		case RPC_ENVELOPE_TYPE_ADMIN:
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_ADMIN");
			byteSent += encodeAdminEnvelope(objOut, (RPCAdminEnvelope) envelope);
			break;	
		default:
			log.warn("ignore invalid RPCEnvelope type");
		}
		
		return byteSent;
	}
	
	protected long encodeAdminEnvelope(ObjectOutputStream objOut,
			RPCAdminEnvelope adminEnv) throws IOException {
		long byteSent = 0L;		
		objOut.writeShort(adminEnv.getEnvelopeType());	
		byteSent +=2L;
		objOut.writeInt(adminEnv.getAdminCommand());
		byteSent +=4L;
		List<RPCObject> rpcObjs = adminEnv.getRpcObjects();
		byteSent += encodeRpcObjects(objOut, rpcObjs);			
		objOut.flush();
		return byteSent;
	}
	
	protected long encodeJavaEnvelope(ObjectOutputStream objOut,
			RPCJavaEnvelope javEnv) throws IOException {
		if(log.isDebugEnabled()) log.debug("encodeJavaEnvelope");
		long byteSent = 0L;		
		objOut.writeShort(javEnv.getEnvelopeType());	
		byteSent +=2L;
		Object jarSrc = javEnv.getJarSource();
		if(log.isDebugEnabled()) log.debug("JarSource:{}", jarSrc);
		if(jarSrc==null){
			if(log.isDebugEnabled()) log.debug("JarSource:nil");
			objOut.writeInt(-1);
			byteSent +=4L;
		}else if(jarSrc instanceof byte[]){
			if(log.isDebugEnabled()) log.debug("JarSource:byte array");
			byte[] jarContent = (byte[]) jarSrc;			
			objOut.writeInt(jarContent.length);
			byteSent +=4L;
			objOut.write(jarContent);
			byteSent +=jarContent.length;
		}			
		byteSent += encodeString(objOut, javEnv.getClassName());		
		List<RPCObject> rpcObjs = javEnv.getRpcObjects();
		byteSent += encodeRpcObjects(objOut, rpcObjs);			
		objOut.flush();
		if(log.isDebugEnabled()) log.debug("end of encodeJavaEnvelope");
		return byteSent;
	}
	
	
	
	protected long encodeStream(ObjectOutputStream objOut,
			RPCStreamEnvelope stream) throws IOException {
		long byteSent = 0L;		
		objOut.writeShort(stream.getEnvelopeType());	
		byteSent +=2L;
		objOut.writeShort(stream.getRpcType());	
		byteSent +=2L;
		
		byteSent += encodeString(objOut, stream.getSource());
		byteSent += encodeString(objOut, stream.getDestination());		
		objOut.writeLong(stream.getLength());
		byteSent +=8L;		
		try{
			InputStream in = stream.stream;
			long max_len = stream.getLength();
			long sent = 0;
			int read = 0;				
			while((read=in.read(BINARY_BUFFER))!=-1 && sent<max_len){
				long diff = max_len - sent;
				if(diff>=read){
					objOut.write(BINARY_BUFFER, 0, read);
					sent += read;	
				}else{
					objOut.write(BINARY_BUFFER, 0, (int)diff);
					sent += diff;
				}							
			}
			if(sent!=max_len){
				throw new IOException(String.format("Not enough data to be sent, requested %d bytes only got %d bytes", max_len, sent));
			}
			byteSent +=sent;
		}finally{
			if(stream.stream!=null) stream.stream.close();
		}		
		objOut.flush();
		return byteSent;
	}
	
	protected long encodeMessage(ObjectOutputStream objOut,
			RPCMessageEnvelope msg) throws IOException 
	{
		long byteSent = 0L;
		objOut.writeShort(msg.getEnvelopeType());	
		byteSent +=2L;
		objOut.writeInt(msg.getMessageLength());
		byteSent +=4L;
		if(msg.getMessageLength()>0){
			objOut.write(msg.getMessage());
			byteSent += msg.getMessageLength();
		}
		objOut.flush();
		return byteSent;
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected long encodeRpcObjects(ObjectOutputStream objOut, List<RPCObject> rpcObjs)  throws IOException
	{
		long byteSent = 0L;
		if(rpcObjs==null){
			objOut.writeInt(RPC_OBJECT_VAL_NIL);  //objectType;
			byteSent +=4L;
			return byteSent;
		}
		objOut.writeInt(rpcObjs.size());
		byteSent +=4L;
		for (RPCObject rpcObj : rpcObjs) {
			objOut.writeShort(rpcObj.getObjectType());  //objectType;
			byteSent +=2L;
			switch (rpcObj.getObjectType()) {
			case RPC_OBJECT_TYPE_BOOLEAN:
				Boolean b = getValue(rpcObj, Boolean.class);					
				if (b == null || !b.booleanValue()) {
					objOut.writeByte(0);
				} else {
					objOut.writeByte(1);
				}
				byteSent +=1L;
				break;
			case RPC_OBJECT_TYPE_INT:
				Integer i = getValue(rpcObj, Integer.class);
				if (i == null) {
					objOut.writeInt(0);
				} else {
					objOut.writeInt(i.intValue());
				}
				byteSent +=4L;
				break;
			case RPC_OBJECT_TYPE_LONG:
				Long l = getValue(rpcObj, Long.class);
				if (l == null) {
					objOut.writeLong(0);
				} else {
					objOut.writeLong(l.longValue());
				}
				byteSent +=8L;
				break;
			case RPC_OBJECT_TYPE_DOUBLE:
				Double d = getValue(rpcObj, Double.class);
				if (d == null) {
					objOut.writeDouble(0);
				} else {
					objOut.writeDouble(d.doubleValue());
				}
				byteSent +=8L;
				break;
			case RPC_OBJECT_TYPE_STRING:
				String s = getValue(rpcObj, String.class);					
				if (s == null) {
					objOut.writeInt(RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				} else {
					byte[] bytes = s.getBytes();						
					objOut.writeInt(bytes.length);
					byteSent +=4L;
					objOut.write(bytes);
					byteSent +=bytes.length;
				}
				break;
			case RPC_OBJECT_TYPE_BINARY:
				encodeBinary(objOut, rpcObj);
				
				break;
			case RPC_OBJECT_TYPE_COLLECTION_BOOLEAN:
				boolean[] bs = (boolean[])rpcObj.getValue();
				if(bs==null){
					objOut.writeInt(RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					objOut.writeInt(bs.length);
					byteSent +=4L;
					for (boolean boolean1 : bs) {
						objOut.writeByte(boolean1?1:0);
						byteSent +=1L;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_COLLECTION_INT:
				int[] ints = (int[])rpcObj.getValue();
				if(ints==null){
					objOut.writeInt(RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					objOut.writeInt(ints.length);
					byteSent +=4L;
					for (int i1 : ints) {
						objOut.writeInt(i1);
						byteSent +=4L;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_COLLECTION_LONG:
				long[] longs = (long[] )rpcObj.getValue();
				if(longs==null){
					objOut.writeInt(RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					objOut.writeInt(longs.length);
					byteSent +=4L;
					for (long l1 : longs) {
						objOut.writeLong(l1);
						byteSent +=8L;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_COLLECTION_DOUBLE:
				double[] ds = (double[])rpcObj.getValue();
				if(ds==null){
					objOut.writeInt(RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					objOut.writeInt(ds.length);
					byteSent +=4L;
					for (double d1 : ds) {
						objOut.writeDouble(d1);
						byteSent +=8L;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_COLLECTION_STRING:
				String[] strs = (String[])rpcObj.getValue();
				if(strs==null){
					objOut.writeInt(RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					objOut.writeInt(strs.length);
					byteSent +=4L;
					for (String s1 : strs) {
						byte[] raw = s1.getBytes();
						objOut.writeInt(raw.length);
						objOut.write(raw);
						byteSent +=raw.length;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_MAP_STRING:
				Map<String, String> map = (Map<String, String>) rpcObj.getValue();
				if(map==null){
					objOut.writeInt(RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{					
					objOut.writeInt(map.size());
					byteSent +=4L;
					for (String s1 : map.keySet()) {
						byte[] key = s1.getBytes();
						String valstr = map.get(s1);
						byte[] val = (valstr==null)?null:valstr.getBytes();						
						objOut.writeInt(key.length);
						byteSent += 4L;
						objOut.write(key);	
						byteSent += key.length;
						if(val==null){
							objOut.writeInt(RPC_OBJECT_VAL_NIL);
							byteSent += 4L;
						}else{
							objOut.writeInt(val.length);
							byteSent += 4L;
							objOut.write(val);
							byteSent += val.length;
						}						
					}	
				}				
				break;
			}

		}
		return byteSent;
	}
	
	

	protected long encodeShellEnvelope(ObjectOutputStream objOut,
			RPCShellEnvelope req) throws IOException {
		long byteSent = 0L;
		objOut.writeShort(req.getEnvelopeType());	//envelopeType;	
		byteSent += 2L;
		long id = req.getEnvelopeId() == 0 ? System.currentTimeMillis() : req
				.getEnvelopeId();		
		objOut.writeLong(id);  //envelopeId (sessionId, requestId)
		byteSent += 8L;
		objOut.writeShort(req.getRpcType());  //rpcType
		byteSent += 2L;
		List<RPCObject> rpcObjs = req.getRpcObjects();
		byteSent += encodeRpcObjects(objOut, rpcObjs);			
		objOut.flush();
		return byteSent;
	}
	
	
	
	

	protected long encodeBinary(ObjectOutputStream objOut, RPCObject rpcObj) throws IOException {
		long byteSent = 0L;
		RPCBinaryObject rcpbinary = (RPCBinaryObject)rpcObj;
		Object val = rpcObj.getValue();
		File f = null;
		byte[] data = null;
		if(val instanceof File){
			f = (File) val;
		}else if(val instanceof String){
			f = new File((String) val);
		}else{
			 data = (byte[])val;
		}
		
		if(f!=null){
			String fname = rcpbinary.getName()==null?f.getCanonicalPath().replaceAll("\\", "/"):rcpbinary.getName();
			byte[] fnames = fname.getBytes();
			objOut.writeInt(fnames.length);
			byteSent += 4L;
			objOut.write(fnames);	
			byteSent += fnames.length;
			long flength = f.length();
			objOut.writeInt((int)flength);
			FileInputStream fin = null;
			try{
				fin = new FileInputStream(f);
				int sent = 0;
				int read = 0;				
				while((read=fin.read(BINARY_BUFFER))!=-1){
					objOut.write(BINARY_BUFFER, 0, read);
					sent += read;					
				}
				if(sent!=f.length()){
					//Warnning message here;
				}
				byteSent += sent;
			}finally{
				if(fin!=null) fin.close();
			}			
		}else{
			String fname = rcpbinary.getName()==null?"":rcpbinary.getName();
			byte[] fnames = fname.getBytes();
			objOut.writeInt(fnames.length);			
			objOut.write(fnames);
			byteSent += 4L;
			if(data==null){
				objOut.writeLong(RPC_OBJECT_VAL_NIL);
			}else{
				objOut.writeLong(data.length);
				objOut.write(data);		
				byteSent += data.length;
			}
		}
		return byteSent;
	}

	@SuppressWarnings("unchecked")
	protected <T> T getValue(RPCObject rpcObj, Class<T> clazz) {
		if (rpcObj.getValue() == null)
			return null;
		if (clazz.isInstance(rpcObj.getValue())) {
			return (T) rpcObj.getValue();
		} else {
			return null;
		}
	}

	
	protected long encodeString(ObjectOutputStream out, String target) throws IOException
	{
		if(target==null){
			out.writeInt(RPC_OBJECT_VAL_NIL);
			return 4;
		}else{
			byte[] raw = target.getBytes();
			out.writeInt(raw.length);
			out.write(raw);
			return raw.length+4;
		}
		
	}
	
	

}
