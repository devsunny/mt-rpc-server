package com.asksunny.protocol.rpc;

import static com.asksunny.protocol.rpc.RPCEnvelope.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.io.utils.StreamCopier;

public class StreamProtocolEncoder implements ProtocolEncoder {

	final static Logger log = LoggerFactory.getLogger(StreamProtocolEncoder.class);
	public static final int MAX_BINARY_BUFFER_SIZE = 1024 * 1024; //1MB;
	public static final byte[]  BINARY_BUFFER = new byte[MAX_BINARY_BUFFER_SIZE];
	
	public long encode(OutputStream out, RPCEnvelope envelope)
			throws IOException {
		long byteSent = 0L;		
		switch(envelope.getEnvelopeType())
		{
		case RPC_ENVELOPE_TYPE_MESSAGE:
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_MESSAGE");
			byteSent += encodeMessage(out, (RPCMessageEnvelope) envelope);
			break;
		case RPC_ENVELOPE_TYPE_SHELL:
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_SHELL");
			byteSent += encodeShellEnvelope(out, (RPCShellEnvelope) envelope);
			break;		
		case RPC_ENVELOPE_TYPE_STREAM:
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_STREAM");
			byteSent += encodeStream(out, (RPCStreamEnvelope) envelope);
			break;
		case RPC_ENVELOPE_TYPE_JAVA:	
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_JAVA");
			byteSent += encodeJavaEnvelope(out, (RPCJavaEnvelope) envelope);
			break;
		case RPC_ENVELOPE_TYPE_ADMIN:
			if(log.isDebugEnabled()) log.debug("encode RPC_ENVELOPE_TYPE_ADMIN");
			byteSent += encodeAdminEnvelope(out, (RPCAdminEnvelope) envelope);
			break;	
		default:
			log.warn("ignore invalid RPCEnvelope type");
		}
		
		return byteSent;
	}
	
	protected void writeShort(OutputStream objOut, short val)  throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(2);
		buf.putShort(val);
		objOut.write(buf.array());
	}
	protected void writeInt(OutputStream objOut, int val)  throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(val);
		objOut.write(buf.array());
	}
	
	protected void writeLong(OutputStream objOut, long val)  throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(val);
		objOut.write(buf.array());
	}
	
	protected void writeDouble(OutputStream objOut, double val)  throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putDouble(val);
		objOut.write(buf.array());
	}
	
	protected long encodeAdminEnvelope(OutputStream objOut,
			RPCAdminEnvelope adminEnv) throws IOException {
		long byteSent = 0L;		
		
		
		writeShort(objOut, adminEnv.getEnvelopeType());	
		byteSent +=2L;
		writeInt(objOut, adminEnv.getAdminCommand());
		byteSent +=4L;
		List<RPCObject> rpcObjs = adminEnv.getRpcObjects();
		byteSent += encodeRpcObjects(objOut, rpcObjs);			
		objOut.flush();
		return byteSent;
	}
	
	protected long encodeJavaEnvelope(OutputStream objOut,
			RPCJavaEnvelope javEnv) throws IOException {
		if(log.isDebugEnabled()) log.debug("encodeJavaEnvelope");
		long byteSent = 0L;		
		writeShort(objOut, javEnv.getEnvelopeType());	
		byteSent +=2L;
		Object jarSrc = javEnv.getJarSource();
		if(log.isDebugEnabled()) log.debug("JarSource:{}", jarSrc);
		if(jarSrc==null){
			if(log.isDebugEnabled()) log.debug("JarSource:nil");
			writeInt(objOut, -1);
			byteSent +=4L;
		}else if(jarSrc instanceof byte[]){
			if(log.isDebugEnabled()) log.debug("JarSource:byte array");
			byte[] jarContent = (byte[]) jarSrc;			
			writeInt(objOut,jarContent.length);
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
	
	
	
	protected long encodeStream(OutputStream objOut,
			RPCStreamEnvelope stream) throws IOException {
		long byteSent = 0L;	
		writeShort(objOut, stream.getEnvelopeType());	
		byteSent +=2L;
		writeShort(objOut, stream.getRpcType());	
		byteSent +=2L;		
		byteSent += encodeString(objOut, stream.getSource());
		byteSent += encodeString(objOut, stream.getDestination());			
		
		if(stream.getRpcType()==RPC_TYPE_RESPONSE && stream.getSource()!=null){
			File f = new File(stream.getSource());
			long l = f.length();
			writeLong(objOut, f.length());
			if(log.isDebugEnabled()) log.debug("Source file size in bytes [{}]", l);
			byteSent +=8L;		
			InputStream fin = null;
			try{
				fin = new FileInputStream(f);				
				long sent = StreamCopier.copy(fin, 0, 0, objOut);
				byteSent +=sent;
			}finally{
				if(fin!=null) fin.close();
			}	
		}else{
			writeLong(objOut, 0);
		}
		if(log.isDebugEnabled()) log.debug("Stream encode complete, total bytes sent [{}]", byteSent);
		objOut.flush();
		return byteSent;
	}
	
	protected long encodeMessage(OutputStream objOut,
			RPCMessageEnvelope msg) throws IOException 
	{
		long byteSent = 0L;
		writeShort(objOut, msg.getEnvelopeType());	
		byteSent +=2L;
		writeInt(objOut,msg.getMessageLength());
		byteSent +=4L;
		if(msg.getMessageLength()>0){
			objOut.write(msg.getMessage());
			byteSent += msg.getMessageLength();
		}
		objOut.flush();
		return byteSent;
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected long encodeRpcObjects(OutputStream objOut, List<RPCObject> rpcObjs)  throws IOException
	{
		long byteSent = 0L;
		if(rpcObjs==null){
			writeInt(objOut,RPC_OBJECT_VAL_NIL);  //objectType;
			byteSent +=4L;
			return byteSent;
		}
		writeInt(objOut,rpcObjs.size());
		byteSent +=4L;
		for (RPCObject rpcObj : rpcObjs) {
			writeShort(objOut, rpcObj.getObjectType());  //objectType;
			byteSent +=2L;
			switch (rpcObj.getObjectType()) {
			case RPC_OBJECT_TYPE_BOOLEAN:
				Boolean b = getValue(rpcObj, Boolean.class);					
				if (b == null || !b.booleanValue()) {
					objOut.write(0);
				} else {
					objOut.write(1);
				}
				byteSent +=1L;
				break;
			case RPC_OBJECT_TYPE_INT:
				Integer i = getValue(rpcObj, Integer.class);
				if (i == null) {
					writeInt(objOut,0);
				} else {
					writeInt(objOut,i.intValue());
				}
				byteSent +=4L;
				break;
			case RPC_OBJECT_TYPE_LONG:
				Long l = getValue(rpcObj, Long.class);
				if (l == null) {
					writeLong(objOut, 0);
				} else {
					writeLong(objOut, l.longValue());
				}
				byteSent +=8L;
				break;
			case RPC_OBJECT_TYPE_DOUBLE:
				Double d = getValue(rpcObj, Double.class);
				if (d == null) {
					writeDouble(objOut, 0);
				} else {
					writeDouble(objOut,d.doubleValue());
				}
				byteSent +=8L;
				break;
			case RPC_OBJECT_TYPE_STRING:
				String s = getValue(rpcObj, String.class);					
				if (s == null) {
					writeInt(objOut,RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				} else {
					byte[] bytes = s.getBytes();						
					writeInt(objOut,bytes.length);
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
					writeInt(objOut,RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					writeInt(objOut,bs.length);
					byteSent +=4L;
					for (boolean boolean1 : bs) {
						objOut.write(boolean1?1:0);
						byteSent +=1L;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_COLLECTION_INT:
				int[] ints = (int[])rpcObj.getValue();
				if(ints==null){
					writeInt(objOut,RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					writeInt(objOut,ints.length);
					byteSent +=4L;
					for (int i1 : ints) {
						writeInt(objOut,i1);
						byteSent +=4L;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_COLLECTION_LONG:
				long[] longs = (long[] )rpcObj.getValue();
				if(longs==null){
					writeInt(objOut,RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					writeInt(objOut,longs.length);
					byteSent +=4L;
					for (long l1 : longs) {
						writeLong(objOut, l1);
						byteSent +=8L;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_COLLECTION_DOUBLE:
				double[] ds = (double[])rpcObj.getValue();
				if(ds==null){
					writeInt(objOut,RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					writeInt(objOut,ds.length);
					byteSent +=4L;
					for (double d1 : ds) {
						writeDouble(objOut,d1);
						byteSent +=8L;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_COLLECTION_STRING:
				String[] strs = (String[])rpcObj.getValue();
				if(strs==null){
					writeInt(objOut,RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{
					writeInt(objOut,strs.length);
					byteSent +=4L;
					for (String s1 : strs) {
						byte[] raw = s1.getBytes();
						writeInt(objOut,raw.length);
						objOut.write(raw);
						byteSent +=raw.length;
					}	
				}
				break;
			case RPC_OBJECT_TYPE_MAP_STRING:
				Map<String, String> map = (Map<String, String>) rpcObj.getValue();
				if(map==null){
					writeInt(objOut,RPC_OBJECT_VAL_NIL);
					byteSent +=4L;
				}else{					
					writeInt(objOut,map.size());
					byteSent +=4L;
					for (String s1 : map.keySet()) {
						byte[] key = s1.getBytes();
						String valstr = map.get(s1);
						byte[] val = (valstr==null)?null:valstr.getBytes();						
						writeInt(objOut,key.length);
						byteSent += 4L;
						objOut.write(key);	
						byteSent += key.length;
						if(val==null){
							writeInt(objOut,RPC_OBJECT_VAL_NIL);
							byteSent += 4L;
						}else{
							writeInt(objOut,val.length);
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
	
	

	protected long encodeShellEnvelope(OutputStream objOut,
			RPCShellEnvelope req) throws IOException {
		long byteSent = 0L;
		writeShort(objOut, req.getEnvelopeType());	//envelopeType;	
		byteSent += 2L;
		long id = req.getEnvelopeId() == 0 ? System.currentTimeMillis() : req
				.getEnvelopeId();		
		writeLong(objOut, id);  //envelopeId (sessionId, requestId)
		byteSent += 8L;
		writeShort(objOut, req.getRpcType());  //rpcType
		byteSent += 2L;
		List<RPCObject> rpcObjs = req.getRpcObjects();
		byteSent += encodeRpcObjects(objOut, rpcObjs);			
		objOut.flush();
		return byteSent;
	}
	
	
	
	

	protected long encodeBinary(OutputStream objOut, RPCObject rpcObj) throws IOException {
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
			writeInt(objOut,fnames.length);
			byteSent += 4L;
			objOut.write(fnames);	
			byteSent += fnames.length;
			long flength = f.length();
			writeInt(objOut,(int)flength);
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
			writeInt(objOut,fnames.length);			
			objOut.write(fnames);
			byteSent += 4L;
			if(data==null){
				writeLong(objOut, RPC_OBJECT_VAL_NIL);
			}else{
				writeLong(objOut, data.length);
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

	
	protected long encodeString(OutputStream out, String target) throws IOException
	{
		if(target==null){
			writeInt(out, RPC_OBJECT_VAL_NIL);
			return 4;
		}else{
			byte[] raw = target.getBytes();
			writeInt(out, raw.length);
			out.write(raw);
			return raw.length+4;
		}
		
	}
	
	

}
