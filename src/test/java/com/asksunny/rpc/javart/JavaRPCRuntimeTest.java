package com.asksunny.rpc.javart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.asksunny.cli.utils.CLICommand;
import com.asksunny.io.utils.StreamCopier;
import com.asksunny.protocol.rpc.RPCBinaryObject;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCJavaEnvelope;
import com.asksunny.protocol.rpc.RPCObject;

public class JavaRPCRuntimeTest {

	@Test
	public void test() throws Exception {
		JavaRPCRuntime rt = new JavaRPCRuntime();
		RPCJavaEnvelope env = new RPCJavaEnvelope();
		env.setClassName("com.asksunny.rpc.rtclient.RPCWorkerTest");		
		RPCEnvelope enveloper =	rt.invoke(env);
		assertNotNull(enveloper.getRpcObjects());
		assertTrue(enveloper.getRpcObjects().size()==1);
		assertEquals("This is returned from server execution", enveloper.getRpcObjects().get(0).getValue());
	}
	
	//java "C:/Users/Sunny Liu/git/mt-rpc-server/src/test/resources/test2.jar" com.asksunny.rpc.rtclient.RPCWorkerJarTest
	@Test
	public void testJar() throws Exception {
		InputStream in = getClass().getResourceAsStream("/test.jar");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		StreamCopier.copy(in, bout);
		byte[] b = bout.toByteArray();
		assertEquals(962, b.length);		
		List<RPCObject> ps  = new ArrayList<RPCObject>();
		ps.add(new RPCBinaryObject().setName("com.asksunny.rpc.rtclient.RPCWorkerJarTest").setValue(b));		
		JavaRPCRuntime rt = new JavaRPCRuntime();	
		RPCJavaEnvelope env = new RPCJavaEnvelope();
		env.setClassName("com.asksunny.rpc.rtclient.RPCWorkerJarTest").setJarSource(b);
		RPCEnvelope enveloper =	rt.invoke(env);
		List<RPCObject> objs =	enveloper.getRpcObjects();
		assertNotNull(objs);
		assertTrue(objs.size()==1);
		assertEquals("This is returned from server execution2", objs.get(0).getValue());
	}
	
	@Test
	public void testEncodeWithJar() throws Exception {
		
		String[] args = new String[]{"C:/Users/Sunny Liu/git/mt-rpc-server/src/test/resources/test.jar", "com.asksunny.rpc.rtclient.RPCWorkerJarTest"};
		CLICommand rcmd = new CLICommand();
		rcmd.setCmdArray(args);		
		RPCJavaEnvelope env = RPCJavaEnvelope.createJavaEnvelope(rcmd);
		
		JavaRPCRuntime rt = new JavaRPCRuntime();	
		RPCEnvelope enveloper =	rt.invoke(env);
		List<RPCObject> objs =	enveloper.getRpcObjects();
		assertNotNull(objs);
		assertTrue(objs.size()==1);
		assertEquals("This is returned from server execution2", objs.get(0).getValue());
	}
	
	

}
