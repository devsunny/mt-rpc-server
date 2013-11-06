package com.asksunny.rpc.shellrt;

import static org.junit.Assert.*;

import org.junit.Test;

public class OSUtilTest {

	@Test
	public void test() {
		
		if(System.getenv("ComSpec")!=null){
			assertTrue(OSUtil.isWindow());
		}else{
			assertTrue(OSUtil.isUnix());
		}
	}

}
