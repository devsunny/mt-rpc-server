package com.asksunny.rpc.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.cli.utils.CLIOptionAnnotationBasedBinder;
import com.asksunny.cli.utils.CLICommand;
import com.asksunny.cli.utils.CLICommandLineParser;
import com.asksunny.cli.utils.annotation.CLIOptionBinding;
import com.asksunny.protocol.rpc.CLIRPCObjectFormatter;
import com.asksunny.protocol.rpc.ProtocolDecodeHandler;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCJavaEnvelope;
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
	
	
	
	
	
	public static void main(String[] args) throws Exception {
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

		RPCClient client = new RPCClient(cli.remoteHost, cli.remotePort,
				cli.ssl);
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
			if (command.equalsIgnoreCase("exit")
					|| command.equalsIgnoreCase("quit")) {
				client.shutdown();
				break;
			}
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
						continue;
					}
					envelope = RPCJavaEnvelope
							.createJavaEnvelope(remoteCommand);
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
				client.sendRequest(envelope);
			}

		}

		ct.join();

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

			break;

		}

	}

	CLIRPCObjectFormatter formatter = new CLIRPCObjectFormatter();

	public void onReceive(RPCEnvelope envelope) {

		System.out.println(formatter.format(envelope.getRpcObjects().get(1)));
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

}
