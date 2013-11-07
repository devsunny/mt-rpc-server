package com.asksunny.io.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class StreamCopier {

	
	public final static long MEM_SIZE_1GB = 1024 * 1024 * 1024;
	public final static long MEM_SIZE_2GB = MEM_SIZE_1GB * 2;
	public final static long MEM_SIZE_3GB = MEM_SIZE_1GB * 3;
	public final static long MEM_SIZE_4GB = MEM_SIZE_1GB * 4;
	public final static long MEM_SIZE_5GB = MEM_SIZE_1GB * 5;
	public final static long MEM_SIZE_10GB = MEM_SIZE_1GB * 10;
	public final static long MEM_SIZE_1MB = 1024 * 1024;
	public final static long MEM_SIZE_10MB = MEM_SIZE_1MB *10;
	public final static long MEM_SIZE_30MB = MEM_SIZE_1MB *30;	
	public final static long MEM_SIZE_1K = 1024;
	
	
	
	public static long copy(InputStream in, OutputStream out) throws IOException
	{
		return copy( in, 0, 0, out);
	}
	
	public static long copy(InputStream in, long offset, long len, OutputStream out) throws IOException
	{
		
		if(offset>0){
			in.skip(offset);
		}
		if(len<=0) len = Long.MAX_VALUE;		
		long sent = 0;		
		while(sent<len){			
			int c = in.read();
			if(c!=-1){
				out.write(c);
				sent++;
			}else{
				break;
			}
		}		
		out.flush();	
		return sent;
	}
	
	
	
	private StreamCopier() {
		
	}

	

}
