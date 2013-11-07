package com.asksunny.rpc.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.cli.utils.CLICommand;
import com.asksunny.cli.utils.CLICommandLineParser;
import com.asksunny.cli.utils.CLIOptionAnnotationBasedBinder;
import com.asksunny.cli.utils.annotation.CLIOptionBinding;
import com.asksunny.protocol.rpc.CLIRPCObjectFormatter;
import com.asksunny.protocol.rpc.ProtocolDecodeHandler;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCJavaEnvelope;
import com.asksunny.protocol.rpc.RPCObject;
import com.asksunny.protocol.rpc.RPCShellEnvelope;
import com.asksunny.protocol.rpc.RPCStreamEnvelope;

public class RPCClientCLI implements ProtocolDecodeHandler {

	final static String RPCSHELL_PROMPT = "RPCClientCli>";
	final static String SHELL = "shell";
	final static String ADMIN = "admin";
	final static String JAVA = "java";
	final static String STREAM = "stream";
	final static String TOREMOTE = "toRemote";
	final static String FROMREMOTE = "fromRemote";
	final static String HELP1 = "help";
	final static String HELP2 = "?";
	
	final static Logger log = LoggerFactory.getLogger(RPCClientCLI.class);
	@CLIOptionBinding(shortOption = 'S', longOption = "server-address", hasValue = true, description = "Server hostname or IP")
	String remoteHost;
	@CLIOptionBinding(shortOption = 'p', longOption = "remote-port", hasValue = true, description = "Server TCP port, no default value this is requested")
	int remotePort;
	@CLIOptionBinding(shortOption = 's', longOption = "ssl", hasValue = false, description = "Enable SSL or not, default no SSL. use Java keytool to genenerate self signed certificate \"keytool -genkey -keystore MtRpcServerKeystore -keyalg RSA\"")
	boolean ssl = false;
	@CLIOptionBinding(shortOption = 'h', longOption = "help", hasValue = false, description = "Print this menu")
	boolean showHelp;
	
	private static final ConcurrentLinkedQueue<String> commandHistory = new ConcurrentLinkedQueue<String>();
	
	
	
	protected static void saveCommandHistory()
	{
		
		PrintWriter bw  = null;
		
		try
		{
			bw  = new PrintWriter(".commandHistory");
			
			String line = null;
			while((line=commandHistory.poll())!=null){				
				bw.println(line);
				System.out.println(line);
			}	
			bw.flush();
		}catch (Exception e) {
			;
		}finally{
			if(bw!=null)
				try {
					bw.close();
				} catch (Exception e) {
					;
				}
		}
	}
	
	protected static void loadCommandHistory()
	{
		
		File f = new File(".commandHistory");		
		if(f.exists()){
			try(FileReader fr = new FileReader(f);
					BufferedReader br  = new BufferedReader(fr))
			{
				String line = null;
				while((line=br.readLine())!=null){
					line = line.trim();
					if(line.length()>0) addCommandToHistory(line);				
				}			
			}catch (Exception e) {
				;
			}			
		}
	}
	
	
	protected static void addCommandToHistory(String command)
	{
		int size = commandHistory.size();
		while(size>99){
			commandHistory.poll();
			size--;
		}
		commandHistory.add(command);
	}
	
	protected static String getCommand(int i)
	{
		Object[] cmds = commandHistory.toArray();		
		int size = cmds.length;		
		if(i<=size){	
			return cmds[size-i].toString();
		}else{			
			System.err.println("Invalid index " + i);
			return null;
		}
	}
	
	
	protected static void showHistory(PrintStream out)
	{
		Object[]  cmds = commandHistory.toArray();
		int size = cmds.length;
		for (int i = 0; i < cmds.length; i++) {
			out.println(String.format("%03d %s",   size-i, cmds[i]));
		}
	}
	
	
	
	public static void main(String[] args) throws Exception {
		loadCommandHistory();
		
		RPCClientCLI cli = new RPCClientCLI();
		Options opts = CLIOptionAnnotationBasedBinder.getOptions(cli);
		CLIOptionAnnotationBasedBinder.bindPosix(opts, args, cli);
		if (cli.remotePort < 1 || cli.showHelp) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp(
					"RPCClientCLI -S rpc_server_name -p port_number [options]",
					opts);
			System.exit(1);
		}

		RPCClient client = new RPCClient(cli.remoteHost, cli.remotePort,  cli.ssl);
		try {
			client.connect();
			client.registerHandler(cli);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		Thread ct = new Thread(client);
		ct.start();

		String command = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.print(RPCSHELL_PROMPT);
		while ((command = reader.readLine()) != null) {
			String rpcCommand = null;			
			if (command.equalsIgnoreCase("exit")
					|| command.equalsIgnoreCase("quit")) {
				saveCommandHistory();
				client.shutdown();
				break;
			}else if(command.equalsIgnoreCase("history")){
				showHistory(System.out);
				System.out.print(RPCSHELL_PROMPT);
			}else if(command.startsWith("!")){				
				String number = command.substring(1);				
				int idx = 0;
				try{
					idx = Integer.valueOf(number).intValue();
				}catch(Exception ex){
					idx = 0;
				}
				if(idx<1 || idx>commandHistory.size()){
					System.err.println("Invalid command history index " + number);
				}else{					
					rpcCommand = getCommand(idx);
					System.out.println(rpcCommand);
				}
			}else{
				rpcCommand = command;
			}			
			if(rpcCommand!=null){				
				try{
					handleCommand(client,  rpcCommand);
					addCommandToHistory(rpcCommand);
				}catch(Throwable t){
					t.printStackTrace(System.err);	
					System.out.print(RPCSHELL_PROMPT);
				}
			}
			
		}

		ct.join();

	}
	
	
	protected static void handleCommand(RPCClient client, String command) throws Exception
	{
		CLICommand[] cmds = CLICommandLineParser.parseCommand(command);
		for (CLICommand remoteCommand : cmds) {
			RPCEnvelope envelope = null;
			String cmd = remoteCommand.peek();			
			if(cmd.equalsIgnoreCase(HELP1)|| cmd.equalsIgnoreCase(HELP2)){
				printHelp(-1);					
			}else if (cmd.equalsIgnoreCase(JAVA)) {
				remoteCommand.shift();
				String ncmd = remoteCommand.peek();
				if(ncmd.equalsIgnoreCase(HELP1)|| ncmd.equalsIgnoreCase(HELP2))
				{
					printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_JAVA);	
					
				}else{					
					envelope = RPCJavaEnvelope
							.createJavaEnvelope(remoteCommand);		
				}
			} else if (cmd.equalsIgnoreCase(STREAM)) {
				remoteCommand.shift();
				String action = remoteCommand.shift();
				if(action.equalsIgnoreCase(HELP1)|| action.equalsIgnoreCase(HELP2))
				{
					printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM);	
					continue;
				}else if (action.equalsIgnoreCase(TOREMOTE)) {
					RPCStreamEnvelope env = new RPCStreamEnvelope();
					env.setRpcType(RPCEnvelope.RPC_TYPE_RESPONSE);
					
					String source = remoteCommand.shift();
					String destination = remoteCommand.shift();						
					if(source!=null && destination!=null){
						File sf = new File(source);
						long length = sf.length();
						env.setSource(source);
						env.setDestination(destination);
						env.setStream(new FileInputStream(sf));
						env.setLength(length);
						envelope = env;
					}else{
						System.err.println("Please provide source and destination path");
						printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM);	
						continue;
					}
				} else if (action.equalsIgnoreCase(FROMREMOTE)) {
					RPCStreamEnvelope env = new RPCStreamEnvelope();
					env.setRpcType(RPCEnvelope.RPC_TYPE_REQUEST);						
					String source = remoteCommand.shift();
					String destination = remoteCommand.shift();	
					if(source!=null && destination!=null){							
						env.setSource(source);
						env.setDestination(destination);
						env.setLength(-1);
						envelope = env;
					}else{
						System.err.println("Please provide source and destination path");
						printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM);	
						continue;
					}
				} else {
					System.err
							.printf("Invalid stream command:%s\n", action);
				}
			} else if (cmd.equalsIgnoreCase(ADMIN)) {
				remoteCommand.shift();
				String action = remoteCommand.shift();
				if(action.equalsIgnoreCase(HELP1)|| action.equalsIgnoreCase(HELP2))
				{
					printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN);	
					continue;
				}
				
				
			} else if (cmd.equalsIgnoreCase(SHELL)) {
				remoteCommand.shift();
				String ncmd = remoteCommand.peek();
				if(ncmd.equalsIgnoreCase(HELP1)|| ncmd.equalsIgnoreCase(HELP2))
				{
					printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_SHELL);	
					continue;
				}					
				envelope = RPCShellEnvelope
						.createShellCommand(remoteCommand);
			} else {
				envelope = RPCShellEnvelope
						.createShellCommand(remoteCommand);
			}
			if(envelope!=null){
				client.sendRequest(envelope);
			}
		}

	}

	protected static void printHelp(int envelopeType) {

		switch (envelopeType) {
		case RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN:			
			break;
		case RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM:
			break;
		case RPCEnvelope.RPC_ENVELOPE_TYPE_SHELL:
			break;
		case RPCEnvelope.RPC_ENVELOPE_TYPE_JAVA:
			break;
		default:
			System.out.println("Available command: java, shell, stream, admin. Type [command] help for detail of each command.");
			break;

		}

	}

	CLIRPCObjectFormatter formatter = new CLIRPCObjectFormatter();

	public void onReceive(RPCEnvelope envelope) {

		if(envelope.getRpcObjects()!=null){
			for (RPCObject rpcObject : envelope.getRpcObjects()) {
				System.out.println(formatter.format(rpcObject));
			}
		}
		System.out.print(RPCSHELL_PROMPT);
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isShowHelp() {
		return showHelp;
	}

	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
	}

	@Override
	public void onSocketIOError(IOException iex) {
		System.err.println("Server shutdown expectly.");		
		saveCommandHistory();
		System.exit(1);		
	}

}
