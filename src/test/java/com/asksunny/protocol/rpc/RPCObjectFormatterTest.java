package com.asksunny.protocol.rpc;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class RPCObjectFormatterTest {

	@Test
	public void testFormateRPCObject() 
	{
		RPCObject obj = new RPCObject(RPCEnvelope.RPC_OBJECT_TYPE_COLLECTION_STRING).setValue(new String[]{"Hello 1", "Hello\n 2", "\"World\"", "ACME"});
		CLIRPCObjectFormatter formatter = new CLIRPCObjectFormatter();
		String str = formatter.format(obj);
		System.out.println(str);
		assertEquals("[\"Hello 1\",\"Hello\\n 2\",\"\\\"World\\\"\",\"ACME\"]", str);
	}
	
	@Test
	public void testFormateRPCObjects() 
	{		
		List<RPCObject> objs = new ArrayList<RPCObject>();		
		RPCObject obj = new RPCObject(RPCEnvelope.RPC_OBJECT_TYPE_COLLECTION_STRING).setValue(new String[]{"Hello 1", "Hello\n 2", "\"World\"", "ACME"});
		RPCObject obj2 = new RPCObject(RPCEnvelope.RPC_OBJECT_TYPE_INT).setValue(1);
		RPCObject obj3 = new RPCObject(RPCEnvelope.RPC_OBJECT_TYPE_BOOLEAN).setValue(false);
		objs.add(obj);
		objs.add(obj2);
		objs.add(obj3);
		CLIRPCObjectFormatter formatter = new CLIRPCObjectFormatter();
		String str = formatter.format(objs);
		System.out.println(str);
		assertEquals("{0->[\"Hello 1\",\"Hello\\n 2\",\"\\\"World\\\"\",\"ACME\"],1->1,2->false}", str);
	}
	
	@Test
	public void testMessageEnvelope() 
	{		
		RPCMessageEnvelope env = new RPCMessageEnvelope();
		env.setMessage("This is message from outter space".getBytes());		
		CLIRPCObjectFormatter formatter = new CLIRPCObjectFormatter();
		String str = formatter.format(env);
		System.out.println(str);
		assertEquals("This is message from outter space", str);
	}
	
	@Test
	public void testResponseEnvelope() 
	{		
		RPCShellEnvelope env = new RPCShellEnvelope();
		env.addRpcObjects("This is response message from galaxy far far away.");
		CLIRPCObjectFormatter formatter = new CLIRPCObjectFormatter();
		String str = formatter.format(env);
		System.out.println(str);
		assertEquals("Response:{0->\"This is response message from galaxy far far away.\"}", str);
	}
	
	
	@Test
	public void testRequestEnvelope() 
	{		
		RPCShellEnvelope env = new RPCShellEnvelope();
		env.addRpcObjects("ls").addRpcObjects("-ltr").addRpcObjects("/home/user/sliu/myfile*.txt");
		
		CLIRPCObjectFormatter formatter = new CLIRPCObjectFormatter();
		String str = formatter.format(env);
		System.out.println(str);
		assertEquals("Request:{0->\"ls\",1->\"-ltr\",2->\"/home/user/sliu/myfile*.txt\"}", str);
	}

}
