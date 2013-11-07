package com.asksunny.protocol.rpc;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.asksunny.protocol.rpc.RPCEnvelope.*;

import org.junit.Test;

import com.asksunny.io.utils.StreamCopier;

public class StreamProtocolEncoderTest {

	@Test
	public void testEncodeString() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCShellEnvelope request = new RPCShellEnvelope();
		request.setRpcType(RPCEnvelope.RPC_TYPE_REQUEST).addRpcObjects("dir");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		
		InputStream in = (new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_STRING, objectType);
		int length = readInt(in);
		assertEquals(3, length);
		byte[] buf = new byte[length];
		readFully(in, buf);
		String cmd = new String(buf);
		assertEquals("dir", cmd);
		
	}
	
	
		
	
	@Test
	public void testEncodeIntCollection() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(new int[]{1,2,3,4,5});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_COLLECTION_INT, objectType);
		int length = readInt(in);
		assertEquals(5, length);		
		for(int i=0; i<length; i++){
			assertEquals(i+1,readInt(in));
		}				
	}
	
	
	@Test
	public void testEncodeNullIntCollection() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_COLLECTION_INT).setValue(null));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);		
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_COLLECTION_INT, objectType);
		int length = readInt(in);
		assertEquals(-1, length);		
		for(int i=0; i<length; i++){
			assertEquals(i+1,readInt(in));
		}
				
	}
	
	
	@Test
	public void testEncodeMap() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();		
		Map<String, String> map = new HashMap<String, String>();
		map.put("test key 1", "test value 1");
		map.put("test key 2", "test value 2");
		map.put("test key 3", "test value 3");
		map.put("test key 4", "test value 4");
		map.put("test key 5", "test value 5");
		map.put("test key 6", "test value 6");
		
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_MAP_STRING).setValue(map));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);		
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_MAP_STRING, objectType);
		int length = readInt(in);
		assertEquals(6, length);		
		for(int i=0; i<length; i++){
			int klen = readInt(in);
			byte[] kbuf = new byte[klen];
			readFully(in, kbuf);
			int vlen = readInt(in);
			byte[] vbuf = new byte[vlen];
			readFully(in, vbuf);
			assertTrue(map.containsKey(new String(kbuf)));
			assertTrue(map.containsValue(new String(vbuf)));
		}
				
	}
	
	@Test
	public void testEncodeNullMap() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_MAP_STRING).setValue(null));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);		
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_MAP_STRING, objectType);
		int length = readInt(in);
		assertEquals(-1, length);
				
	}
	
	
	@Test
	public void testEncodeInt() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_INT).setValue(1000));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_INT, objectType);
		int val = readInt(in);
		assertEquals(1000, val);		
			
	}
	
	@Test
	public void testEncodeBoolean() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_BOOLEAN).setValue(true));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_BOOLEAN, objectType);
		int val = in.read();
		assertEquals(1, val);		
				
	}
	
	
	@Test
	public void testEncodeLong() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_LONG).setValue(10001L));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_LONG, objectType);
		long val = readLong(in);
		assertEquals(10001L, val);		
		
	}
	
	
	@Test
	public void testEncodeDouble() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_DOUBLE).setValue(102.25));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_SHELL, envelopeType);
		long envelopeId = readLong(in);
		System.out.println("envelopeid" + envelopeId);
		short requestType = readShort(in);
		assertEquals(RPC_TYPE_REQUEST, requestType);
		assertEquals(1, readInt(in));
		short objectType = readShort(in);
		assertEquals(RPC_OBJECT_TYPE_DOUBLE, objectType);
		double val =readDouble( in);
		assertEquals(102.25, val, 0.001);		
			
	}
	
	
	@Test
	public void testEncodeMessage() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCMessageEnvelope request = new RPCMessageEnvelope();
		request.setMessage("This is my message to server".getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_MESSAGE, envelopeType);		
		
		int length = readInt(in);
		assertEquals("This is my message to server".getBytes().length, length);
		byte[] buf = new byte[length];
		readFully(in, buf);
		String cmd = new String(buf);
		assertEquals("This is my message to server", cmd);			
	}
	
	
	@Test
	public void testEncodeStream() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCStreamEnvelope()).setLength(5).setStream(new ByteArrayInputStream("HELLO WORLD".getBytes()));		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_STREAM, envelopeType);		
		assertEquals(RPC_TYPE_REQUEST, readShort(in));		
		int src_len = readInt(in);
		assertEquals(-1, src_len);	
		int dest_len = readInt(in);
		assertEquals(-1, dest_len);		
		long length = readLong(in);
		assertEquals("HELLO".getBytes().length, length);
		in.skip(length);	
				
	}
	
	@Test
	public void testEncodeJava() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCJavaEnvelope request = (new RPCJavaEnvelope());
		InputStream jarin = getClass().getResourceAsStream("/test.jar");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		StreamCopier.copy(jarin, bout);
		byte[] b = bout.toByteArray();
		assertEquals(962, b.length);		
		request.setJarSource(b).setClassName("com.asksunny.rpc.rtclient.RPCWorkerJarTest");
		
		request.addRpcObjects("This is parameter to Java prog.");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_JAVA, envelopeType);		
		int src_len = readInt(in);
		assertEquals(962, src_len);	
		byte[] rawdata2 = new byte[src_len];
		readFully(in, rawdata2);		
		int clzn_len = readInt(in);
		assertEquals("com.asksunny.rpc.rtclient.RPCWorkerJarTest".getBytes().length, clzn_len);		
		String s = readString(in, clzn_len);
		assertEquals("com.asksunny.rpc.rtclient.RPCWorkerJarTest", s);	
		assertEquals(1, readInt(in));
		assertEquals(RPC_OBJECT_TYPE_STRING, readShort(in));
		int len = readInt(in);
		assertEquals("This is parameter to Java prog.".getBytes().length, len);
		assertEquals("This is parameter to Java prog.", readString(in, len));
	}

	protected String readString(InputStream objIn, int length) throws IOException
	{
		byte[] buf = new byte[length];
		readFully(objIn, buf);
		return new String(buf);
	}
	
	@Test
	public void testEncodeAdmin() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCAdminEnvelope request = (new RPCAdminEnvelope());
		request.setAdminCommand(RPCAdminCommand.SHUTDOWN);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		
		InputStream in = new ByteArrayInputStream(rawdata);
		assertNotNull(in);
		short envelopeType = readShort(in);
		assertEquals(RPC_ENVELOPE_TYPE_ADMIN, envelopeType);		
		int src_len = readInt(in);
		assertEquals(RPCAdminCommand.SHUTDOWN.getValue(), src_len);	
		int args_len = readInt(in);
		assertEquals(-1, args_len);	
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


}
