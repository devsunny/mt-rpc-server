package com.asksunny.cli.utils;

public class CLIOptionBindingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	

	public CLIOptionBindingException(String arg0) {
		super(arg0);
		
	}

	public CLIOptionBindingException(Throwable arg0) {
		super(arg0);
		
	}

	public CLIOptionBindingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

	public CLIOptionBindingException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		
	}

}
