package com.asksunny.cli.utils;

import java.util.ArrayList;

public class CLICommandLineParser {

	public static CLICommand[] parseCommand(String cmdLine) {
		ArrayList<ArrayList<String>> cmds =parseCommandText(cmdLine);
		 CLICommand[] cmdarryss = new  CLICommand[cmds.size()] ;
		for(int i=0; i<cmds.size(); i++){
			ArrayList<String> cmd = cmds.get(i);
			String[] cmdarray = new String[cmd.size()];
			cmd.toArray(cmdarray);
			cmdarryss[i] = (new CLICommand()).setCmdArray(cmdarray);
		}		 
		 return cmdarryss;
	}
	
	protected static ArrayList<ArrayList<String>> parseCommandText(String cmdLine) {
		ArrayList<ArrayList<String>> cmds = new ArrayList<ArrayList<String>>();
		ArrayList<String> cmd = new ArrayList<String>();

		int size = cmdLine.length();
		
		StringBuilder buf  = new StringBuilder();
		for (int i = 0; i < size; i++) {
			char c = cmdLine.charAt(i);
			switch (c) {
			case '"':
				boolean stop = false;
				for(int j=i+1; j<size; j++)
				{
					char c2 = cmdLine.charAt(j);					
					switch(c2)
					{
					case '\\':
						if(j+1<size){
							char nc = cmdLine.charAt(j+1);
							switch(nc)
							{
							case 't':
								buf.append("\t");
								break;
							case '\\':
								buf.append("\\");
								break;
							case 'n':
								buf.append("\n");
								break;
							case '"':
								buf.append('"');
								break;
							default:
								buf.append(c2).append(nc);
								break;
							}
							j = j+1;
						}else{
							buf.append(c2);
						}
						break;
					case '"':
						stop = true;
						break;
					default:
						buf.append(c2);
						break;					
					}
					if(stop) {						
						i=j;
						break;	
					}
				}
				break;
			case '\'':
				boolean stop2 = false;
				for(int j=i+1; j<size; j++)
				{
					
					char c2 = cmdLine.charAt(j);
					switch(c2)
					{
					case '\\':
						if(j+1<size){
							char nc = cmdLine.charAt(j+1);
							switch(nc)
							{
							case 't':
								buf.append("\t");
								break;
							case '\\':
								buf.append("\\");
								break;
							case 'n':
								buf.append("\n");
								break;
							case '\'':
								buf.append('\'');
								break;
							default:
								buf.append(c2).append(nc);
								break;
							}
							j = j+1;
						}else{
							buf.append(c2);
						}
						break;
					case '\'':
						stop2 = true;
						break;
					default:
						buf.append(c2);
						break;					
					}
					if(stop2){
						i=j;
						break;
					}
				}
				break;
			case '\t':
			case ' ':
				if(buf.length()>0){
					cmd.add(buf.toString());
					buf.setLength(0);
				}
				break;
			case ';':
				if(buf.length()>0)	{
					cmd.add(buf.toString());
					buf.setLength(0);
				}
				if(cmd.size()>0){
					cmds.add(cmd);
					cmd = new ArrayList<String>();
				}				
				break;
			default:
				buf.append(c);
				break;
			}
			if(i==size-1){				
				if(buf.length()>0)	{
					cmd.add(buf.toString());
				}
				if(cmd.size()>0) cmds.add(cmd);					
			}
		}
		return cmds;
	}
	
	public static void main(String[]  args)
	{
		 CLICommand[]  cmds = parseCommand("cmd.exe /c \"dir src\\\"d \";exit");
		for (CLICommand cmd : cmds) {
			for (String string : cmd.getCmdArray()) {
				System.out.println(string);
			}
			System.out.println("---------------------------------");
		}
	}

}
