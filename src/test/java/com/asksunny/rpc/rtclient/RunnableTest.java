package com.asksunny.rpc.rtclient;

public class RunnableTest implements Runnable {

	public RunnableTest() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		System.out.println("Running in runnable: RunnableTest");
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			;
		}
		System.out.println("Awake after 5 second.");
	}

}
