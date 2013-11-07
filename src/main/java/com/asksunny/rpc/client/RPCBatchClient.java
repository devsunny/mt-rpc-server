package com.asksunny.rpc.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.cli.utils.CLICommand;
import com.asksunny.cli.utils.CLIOptionAnnotationBasedBinder;
import com.asksunny.cli.utils.annotation.CLIOptionBinding;
import com.asksunny.protocol.rpc.CLIRPCObjectFormatter;
import com.asksunny.protocol.rpc.ProtocolDecodeHandler;
import com.asksunny.protocol.rpc.RPCEnvelope;
import com.asksunny.protocol.rpc.RPCJavaEnvelope;
import com.asksunny.protocol.rpc.RPCObject;
import com.asksunny.protocol.rpc.RPCShellEnvelope;
import com.asksunny.protocol.rpc.RPCStreamEnvelope;
import com.asksunny.protocol.rpc.StreamProtocolDecoder;
import com.asksunny.protocol.rpc.StreamProtocolEncoder;

public class RPCBatchClient {

	final static String SHELL = "shell";
	final static String ADMIN = "admin";
	final static String JAVA = "java";
	final static String STREAM = "stream";
	final static String TOREMOTE = "toRemote";
	final static String FROMREMOTE = "fromRemote";
	final static String HELP1 = "help";
	final static String HELP2 = "?";

	final static Logger log = LoggerFactory.getLogger(RPCBatchClient.class);
	@CLIOptionBinding(shortOption = 'S', longOption = "server-address", hasValue = true, description = "Server hostname or IP")
	String remoteHost;
	@CLIOptionBinding(shortOption = 'p', longOption = "remote-port", hasValue = true, description = "Server TCP port, no default value this is requested")
	int remotePort;
	@CLIOptionBinding(shortOption = 's', longOption = "ssl", hasValue = false, description = "Enable SSL or not, default no SSL. use Java keytool to genenerate self signed certificate \"keytool -genkey -keystore MtRpcServerKeystore -keyalg RSA\"")
	boolean ssl = false;
	@CLIOptionBinding(shortOption = 'h', longOption = "help", hasValue = false, description = "Print this menu")
	boolean showHelp;

	Socket clientSocket;
	InputStream sin = null;
	OutputStream sout = null;

	AtomicLong state = new AtomicLong(0L);
	StreamProtocolDecoder decoder = new StreamProtocolDecoder();
	StreamProtocolEncoder encoder = new StreamProtocolEncoder();

	public void connect() throws IOException {
		if (state.compareAndSet(0, 1)) {
			this.clientSocket = connectToRemoteHost();
			sin = this.clientSocket.getInputStream();
			sout = this.clientSocket.getOutputStream();
		}
	}

	public RPCEnvelope sendRequest(RPCEnvelope req) throws IOException {
		RPCEnvelope env = null;
		try {
			encoder.encode(sout, req);
			env = decoder.decodeNow(sin);
			return env;
		} catch (IOException iex) {
			shutdown();
		} catch (Exception ex) {
			log.warn("Unexpected err", ex);
			env = null;
		}
		return env;
	}

	public void sendRequest(RPCEnvelope req, ProtocolDecodeHandler handler)
			throws IOException {
		try {
			encoder.encode(sout, req);
			RPCEnvelope env = decoder.decodeNow(sin);
			handler.onReceive(env);
		} catch (IOException iex) {
			shutdown();
		} catch (Exception ex) {
			log.warn("Unexpected err", ex);
		}

	}

	public void shutdown() {
		if (sin != null) {
			try {
				sin.close();
			} catch (Exception ex) {
				;
			}
			sin = null;
		}
		if (sout != null) {
			try {
				sout.close();
			} catch (Exception ex) {
				;
			}
			sout = null;
		}

		if (clientSocket != null) {
			try {
				clientSocket.close();
			} catch (Exception ex) {
				;
			}
			clientSocket = null;
		}
	}

	protected Socket connectToRemoteHost() throws IOException {
		Socket client = null;
		if (ssl) {
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(
					this.remoteHost, this.remotePort);
			client = sslsocket;
		} else {
			SocketFactory socketfactory = (SocketFactory) SocketFactory
					.getDefault();
			Socket sslsocket = (SSLSocket) socketfactory.createSocket(
					this.remoteHost, this.remotePort);
			client = sslsocket;
		}
		client.setSoTimeout(30 * 1000);
		client.setKeepAlive(true);
		return client;
	}

	public RPCBatchClient() {
		super();
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	
	public void execute(String[] args) throws Exception
	{
		CLICommand remoteCommand = new CLICommand();
		remoteCommand.setCmdArray(args);
		RPCEnvelope request =  createEnvelope(remoteCommand);
		RPCEnvelope response = sendRequest(request);
		if(response!=null && response.getRpcObjects()!=null){
			CLIRPCObjectFormatter formatter = new CLIRPCObjectFormatter();
			for (RPCObject rpcObject : response.getRpcObjects()) {
				System.out.println(formatter.format(rpcObject));
			}
		}else{
			System.out.println();
		}
	}
	
	
	protected RPCEnvelope createEnvelope(CLICommand remoteCommand)
			throws IOException {
		RPCEnvelope envelope = null;

		String cmd = remoteCommand.peek();
		if (cmd.equalsIgnoreCase(JAVA)) {
			remoteCommand.shift();
			String ncmd = remoteCommand.peek();
			if (ncmd.equalsIgnoreCase(HELP1) || ncmd.equalsIgnoreCase(HELP2)) {
				printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_JAVA);

			} else {
				envelope = RPCJavaEnvelope.createJavaEnvelope(remoteCommand);
			}
		} else if (cmd.equalsIgnoreCase(STREAM)) {
			remoteCommand.shift();
			String action = remoteCommand.shift();
			if (action.equalsIgnoreCase(HELP1)
					|| action.equalsIgnoreCase(HELP2)) {
				printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM);

			} else if (action.equalsIgnoreCase(TOREMOTE)) {
				RPCStreamEnvelope env = new RPCStreamEnvelope();
				env.setRpcType(RPCEnvelope.RPC_TYPE_RESPONSE);

				String source = remoteCommand.shift();
				String destination = remoteCommand.shift();
				if (source != null && destination != null) {
					File sf = new File(source);
					long length = sf.length();
					env.setSource(source);
					env.setDestination(destination);
					env.setStream(new FileInputStream(sf));
					env.setLength(length);
					envelope = env;
				} else {
					System.err
							.println("Please provide source and destination path");
					printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM);

				}
			} else if (action.equalsIgnoreCase(FROMREMOTE)) {
				RPCStreamEnvelope env = new RPCStreamEnvelope();
				env.setRpcType(RPCEnvelope.RPC_TYPE_REQUEST);
				String source = remoteCommand.shift();
				String destination = remoteCommand.shift();
				if (source != null && destination != null) {
					env.setSource(source);
					env.setDestination(destination);
					env.setLength(-1);
					envelope = env;
				} else {
					System.err
							.println("Please provide source and destination path");
					printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM);

				}
			} else {
				System.err.printf("Invalid stream command:%s\n", action);
			}
		} else if (cmd.equalsIgnoreCase(ADMIN)) {
			remoteCommand.shift();
			String action = remoteCommand.shift();
			if (action.equalsIgnoreCase(HELP1)
					|| action.equalsIgnoreCase(HELP2)) {
				printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN);

			}

		} else if (cmd.equalsIgnoreCase(SHELL)) {
			remoteCommand.shift();
			String ncmd = remoteCommand.peek();
			if (ncmd.equalsIgnoreCase(HELP1) || ncmd.equalsIgnoreCase(HELP2)) {
				printHelp(RPCEnvelope.RPC_ENVELOPE_TYPE_SHELL);

			}
			envelope = RPCShellEnvelope.createShellCommand(remoteCommand);
		} else {
			envelope = RPCShellEnvelope.createShellCommand(remoteCommand);
		}
		return envelope;
	}

	protected void printHelp(int envelopeType) {
		switch (envelopeType) {
		case RPCEnvelope.RPC_ENVELOPE_TYPE_ADMIN:
			System.err
			.println("Available command: admin ping|echo|heartbeat|shutdown");
			break;
		case RPCEnvelope.RPC_ENVELOPE_TYPE_STREAM:
			System.err
			.println("Available command: stream toRemote|fromRemote");
			break;
		case RPCEnvelope.RPC_ENVELOPE_TYPE_SHELL:
			System.err
			.println("Available command: shell shell_command|shell_script");
			break;
		case RPCEnvelope.RPC_ENVELOPE_TYPE_JAVA:
			System.err
			.println("Available command: [java path_jar_file] class_name parameter_list_form_class");
			break;
		default:
			System.err
					.println("Available command: java, shell, stream, admin. Type [command] help for detail of each command.");
			break;

		}

	}

	public static void main(String[] args) throws Exception {
		RPCBatchClient client = new RPCBatchClient();
		Options opts = CLIOptionAnnotationBasedBinder.getOptions(client);
		CommandLine cmdline = CLIOptionAnnotationBasedBinder.bindPosix(opts,
				args, client);
		if (client.remotePort < 1 || client.showHelp) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp(
					"RPCBatchClient -S rpc_server_name -p port_number [options] remote_command_list",
					opts);
			System.exit(1);
		}
		String[] rargs = cmdline.getArgs();

		try {
			client.connect();
			client.execute(rargs);
		} finally {
			client.shutdown();
		}

	}

}
