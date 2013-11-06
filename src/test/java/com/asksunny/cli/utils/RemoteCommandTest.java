package com.asksunny.cli.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class RemoteCommandTest {

	@Test
	public void testPeek() {
		String[] cmds = new String[]{"shell", "dir", "c:\\brother"};
		CLICommand command = new CLICommand();
		command.setCmdArray(cmds);
		assertEquals("shell", command.peek());		
	}
	
	@Test
	public void testGetCommandArray() {
		String[] cmds = new String[]{"shell", "dir", "/W", "c:\\brother"};
		CLICommand command = new CLICommand();
		command.setCmdArray(cmds);
		assertEquals("shell", command.peek());	
		assertEquals("shell", command.shift());
		assertArrayEquals(new String[]{"dir", "/W", "c:\\brother"}, command.getCmdArray());
	}
	
	@Test
	public void testShift() {
		String[] cmds = new String[]{"java", "C:\\Users\\sunny\\devel\\scalaIDE302\\workspace\\mt-rpc-server\\src\\test\\resources\\test.jar", "com.asksunny.rpc.rtclient.RPCWorkerJarTest"};
		CLICommand command = new CLICommand();
		command.setCmdArray(cmds);
		assertEquals("java", command.peek());
		assertEquals("java", command.shift());
		command.shift();
		assertEquals("com.asksunny.rpc.rtclient.RPCWorkerJarTest", command.peek());
	}

}
