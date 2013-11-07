package com.asksunny.protocol.rpc;

import static com.asksunny.protocol.rpc.RPCEnvelope.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.asksunny.cli.utils.CLICommand;
import com.asksunny.io.utils.StreamCopier;

public class StreamProtocolDecoderTest {

	@Test
	public void testDecodeString() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCShellEnvelope request = new RPCShellEnvelope();
		request.setRpcType(RPCEnvelope.RPC_TYPE_REQUEST).addRpcObjects("dir");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals("dir", decoded.getRpcObjects().get(0).getValue());
		
	}
	
	
		
	
	@Test
	public void testDecodeIntCollection() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(new int[]{1,2,3,4,5});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals(RPC_OBJECT_TYPE_COLLECTION_INT, decoded.getRpcObjects().get(0).getObjectType());
		int[] param = (int[])decoded.getRpcObjects().get(0).getValue();			
		assertArrayEquals(new int[]{1,2,3,4,5}, param);
		
	}
	
	
	@Test
	public void testDecodeNullIntCollection() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_COLLECTION_INT).setValue(null));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);		
		
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals(RPC_OBJECT_TYPE_COLLECTION_INT, decoded.getRpcObjects().get(0).getObjectType());
		assertNull(decoded.getRpcObjects().get(0).getValue());
				
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDecodeMap() throws IOException{
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
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);		
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals(RPC_OBJECT_TYPE_MAP_STRING, decoded.getRpcObjects().get(0).getObjectType());
		Map<String, String> maps = (Map<String, String>)decoded.getRpcObjects().get(0).getValue();
		assertNotNull(maps.get("test key 6"));
		assertNull(maps.get("test key 6 888"));		
		assertEquals("test value 4", maps.get("test key 4"));
				
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDecodeNullMap() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_MAP_STRING).setValue(null));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);		
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals(RPC_OBJECT_TYPE_MAP_STRING, decoded.getRpcObjects().get(0).getObjectType());
		Map<String, String> maps = (Map<String, String>)decoded.getRpcObjects().get(0).getValue();
		assertNull(maps);
		
				
	}
	
	
	@Test
	public void testDecodeInt() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_INT).setValue(1000));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals(RPC_OBJECT_TYPE_INT, decoded.getRpcObjects().get(0).getObjectType());
	
		assertEquals(1000, ((Integer)decoded.getRpcObjects().get(0).getValue()).intValue());	

			
	}
	
	@Test
	public void testDecodeBoolean() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_BOOLEAN).setValue(true));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals(RPC_OBJECT_TYPE_BOOLEAN, decoded.getRpcObjects().get(0).getObjectType());
	
		assertEquals(true, ((Boolean)decoded.getRpcObjects().get(0).getValue()).booleanValue());
				
	}
	
	
	@Test
	public void testDecodeLong() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_LONG).setValue(10001L));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals(RPC_OBJECT_TYPE_LONG, decoded.getRpcObjects().get(0).getObjectType());
	
		assertEquals(10001L, ((Long)decoded.getRpcObjects().get(0).getValue()).longValue());
		
	}
	
	
	@Test
	public void testDecodeDouble() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCShellEnvelope())
				.setRpcType(RPC_TYPE_REQUEST)
				.addRpcObjects(RPCObject.newInstance(RPC_OBJECT_TYPE_DOUBLE).setValue(102.25));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCShellEnvelope decoded = (RPCShellEnvelope)  decoder.decodeNow(in);
		assertEquals(RPCEnvelope.RPC_TYPE_REQUEST, decoded.getRpcType());
		assertEquals(RPC_OBJECT_TYPE_DOUBLE, decoded.getRpcObjects().get(0).getObjectType());	
		assertEquals(102.25, ((Double)decoded.getRpcObjects().get(0).getValue()).doubleValue(), 0.001);
			
	}
	
	
	@Test
	public void testDecodeMessage() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCMessageEnvelope request = new RPCMessageEnvelope();
		request.setMessage("This is my message to server".getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCMessageEnvelope decoded = (RPCMessageEnvelope)  decoder.decodeNow(in);		
		assertNotNull(decoded);	
		assertEquals("This is my message to server", new String(decoded.getMessage()));	
	}
	
	
	@Test
	public void testDecodeStream() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCEnvelope request = (new RPCStreamEnvelope()).setLength(5).setStream(new ByteArrayInputStream("HELLO WORLD".getBytes()));		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCStreamEnvelope decoded = (RPCStreamEnvelope)  decoder.decodeNow(in);		
		assertNotNull(decoded);	
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		StreamCopier.copy( decoded.getStream(), bo) ;
		assertEquals("HELLO", new String(bo.toByteArray()));	
	}
	
	@Test
	public void testDecodeJava() throws IOException{
		
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
		
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCJavaEnvelope decoded = (RPCJavaEnvelope)  decoder.decodeNow(in);		
		assertNotNull(decoded);	
		
		byte[] rawdata2 = ( byte[])decoded.getJarSource();
		assertEquals(962, rawdata2.length);	
		assertEquals("com.asksunny.rpc.rtclient.RPCWorkerJarTest", decoded.getClassName());	
		assertEquals("This is parameter to Java prog.", decoded.getRpcObjects().get(0).getValue());
	}
	
	
	@Test
	public void testDecodeJava2() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		String[] args = new String[]{"C:/Users/Sunny Liu/git/mt-rpc-server/src/test/resources/test.jar", "com.asksunny.rpc.rtclient.RPCWorkerJarTest"};
		CLICommand rcmd = new CLICommand();
		rcmd.setCmdArray(args);		
		RPCJavaEnvelope request = RPCJavaEnvelope.createJavaEnvelope(rcmd);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);			
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCJavaEnvelope decoded = (RPCJavaEnvelope)  decoder.decodeNow(in);		
		assertNotNull(decoded);			
		byte[] rawdata2 = ( byte[])decoded.getJarSource();
		assertEquals(962, rawdata2.length);	
		assertEquals("com.asksunny.rpc.rtclient.RPCWorkerJarTest", decoded.getClassName());	
		
	}

	protected String readString(ObjectInputStream objIn, int length) throws IOException
	{
		byte[] buf = new byte[length];
		objIn.readFully(buf);
		return new String(buf);
	}
	
	@Test
	public void testDecodeAdmin() throws IOException{
		StreamProtocolEncoder encoder = new StreamProtocolEncoder();
		RPCAdminEnvelope request = (new RPCAdminEnvelope());
		request.setAdminCommand(RPCAdminCommand.SHUTDOWN);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encoder.encode(out, request);		
		byte[] rawdata = out.toByteArray();
		assertNotNull(rawdata);
		
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rawdata));
		assertNotNull(in);
		
		StreamProtocolDecoder decoder = new StreamProtocolDecoder();
		RPCAdminEnvelope decoded = (RPCAdminEnvelope)  decoder.decodeNow(in);		
		assertNotNull(decoded);	
		
		assertEquals(RPCAdminCommand.SHUTDOWN.getValue(), decoded.getAdminCommand());	
		assertNull(decoded.getRpcObjects());
		
	}


}
