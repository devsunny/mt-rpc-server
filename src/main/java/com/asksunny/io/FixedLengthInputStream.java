package com.asksunny.io;

import java.io.IOException;
import java.io.InputStream;

public class FixedLengthInputStream extends InputStream {

	InputStream src = null;
	long length = 0L;
	long read = 0L;
	
	
	public FixedLengthInputStream(InputStream src, long length)
	{
		this.src = src;
		this.length = length;
	}
	
	@Override
	public int read() throws IOException {
		if(read==length) return -1;
		read++;
		return src.read();
	}

	@Override
	public void close() throws IOException {
		;  //user can not close underline stream from this wrapper;
	}
	
	
	
}
