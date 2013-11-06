package com.asksunny.rpc.shellrt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;

public final class OSUtil 
{
	static String os = System.getProperty("os.name");
	
	public static boolean isWindow()
	{
		
		return os.toLowerCase().indexOf("windows")!=-1;
	}
	public static boolean isUnix()
	{
		
		return os.toLowerCase().indexOf("windows")==-1;
	}
	
	public static void inspectSystem()
	{		
		System.out.println(inspectJVM());		
	}
	
	public static String inspectJVM()
	{
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
		Enumeration<?> names = System.getProperties().propertyNames();
		out.println("####---------------------System Propertis  starts here-----------------------");
		while(names.hasMoreElements()){
			String key = (String)names.nextElement();
			out.println(String.format("%s=%s", key, System.getProperty(key)));
		}
		out.println("###---------------------System Propertis ends here -----------------------");
		out.println("###---------------------System Environments starts here -----------------------");
		Map<String, String> envs =  System.getenv();
		for(String key: envs.keySet())
		{
			out.println(String.format("%s=%s", key, System.getenv(key)));
		}
		out.println("###---------------------System Environments ends here -----------------------");
		
		return sw.toString();
	}
	
	
	private OSUtil(){};
}
