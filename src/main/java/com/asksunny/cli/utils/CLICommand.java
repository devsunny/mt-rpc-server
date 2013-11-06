package com.asksunny.cli.utils;

public class CLICommand 
{
	String[] cmdArray;
	int current_pos = 0;

	public String[] getCmdArray() {
		String[] cmds = null;		
		if(cmdArray!=null && cmdArray.length>0 && current_pos<cmdArray.length){
			int len = cmdArray.length - current_pos;
			cmds = new String[len];
			System.arraycopy(cmdArray, current_pos, cmds, 0, len);
		}else{
			cmds = new String[0];
		}		
		return cmds;
	}

	public CLICommand setCmdArray(String[] cmdArray) {
		this.cmdArray = cmdArray;
		return this;
	}
	
	public String peek()
	{
		if(cmdArray!=null && cmdArray.length>0){
			return cmdArray[current_pos];
		}else{
			return null;
		}
	}
	
	public String shift()
	{
		if(cmdArray!=null && cmdArray.length>0 && current_pos<cmdArray.length){
			return cmdArray[current_pos++];
		}else{
			return null;
		}
	}
	
}
