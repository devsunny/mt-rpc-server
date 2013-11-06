package com.asksunny.io.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class StrackTraceUtil {

	
	public static String getStackTraceAsString(Throwable t)
	{
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	
	
	private StrackTraceUtil(){}
}
