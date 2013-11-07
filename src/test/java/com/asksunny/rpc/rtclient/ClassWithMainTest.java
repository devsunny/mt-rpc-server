package com.asksunny.rpc.rtclient;

public class ClassWithMainTest {

	public ClassWithMainTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		int i = 0;
		for(String arg: args)
		{
			System.out.printf("received argument at [%d] %s\n", i++, arg );
		}

	}

}
