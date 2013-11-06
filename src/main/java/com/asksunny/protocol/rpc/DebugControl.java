package com.asksunny.protocol.rpc;

import java.util.concurrent.atomic.AtomicInteger;

public final class DebugControl 
{
	public static final int DEBUG_LEVEL_NORMAL = 0;
	public static final int DEBUG_LEVEL_VERBOSE = 1;
	public static final int DEBUG_LEVEL_EXTREMELY_VERBOSE = 2;
	
	public static final AtomicInteger DEBUG_LEVEL = new AtomicInteger(DEBUG_LEVEL_NORMAL);
	
	public static void setDebugLevel(int val)
	{
		int v = (val>DEBUG_LEVEL_EXTREMELY_VERBOSE)?DEBUG_LEVEL_EXTREMELY_VERBOSE:val;
		v = (v<DEBUG_LEVEL_NORMAL)?DEBUG_LEVEL_NORMAL:v;
		DEBUG_LEVEL.set(v);
	}
	
	public static int getDebugLevel()
	{
		return DEBUG_LEVEL.get();
	}
	
	
	private DebugControl(){}
}
