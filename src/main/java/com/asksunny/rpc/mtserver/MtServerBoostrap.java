package com.asksunny.rpc.mtserver;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asksunny.cli.utils.CLIOptionAnnotationBasedBinder;
import com.asksunny.cli.utils.annotation.CLIOptionBinding;

public class MtServerBoostrap {

	final static Logger log = LoggerFactory.getLogger(MtServerBoostrap.class);
	@CLIOptionBinding(shortOption='b', longOption="binding-address", hasValue=true, description="Binding Address for server, default bind to all interface 0.0.0.0")
	String bindingAddress; 
	@CLIOptionBinding(shortOption='p', longOption="binding-port", hasValue=true, description="Binding tcp port for server, no default value this is requested")
	int port; 
	@CLIOptionBinding(shortOption='B', longOption="socket-backlog", hasValue=true, description="backlog number of tcp socket, default 200")
	int tcpBackLog = 200;
	@CLIOptionBinding(shortOption='s', longOption="ssl", hasValue=false, description="Enable SSL or not, default no SSL. use Java keytool to genenerate self signed certificate \"keytool -genkey -keystore MtRpcServerKeystore -keyalg RSA\"")
	boolean ssl;
	@CLIOptionBinding(shortOption='h', longOption="help", hasValue=false, description="Print this menu")
	boolean showHelp;
	@CLIOptionBinding(shortOption='t', longOption="thread-pool", hasValue=true, description="Specify max number of thread used to execute client request.")
	int maxThread = 10;
	
	
	public static void main(String[] args) {
		MtServerBoostrap boostrap = new MtServerBoostrap();
		Options opts = CLIOptionAnnotationBasedBinder.getOptions(boostrap);
		CLIOptionAnnotationBasedBinder.bindPosix(opts, args, boostrap);
		if(boostrap.getPort()<1 || boostrap.showHelp){
			HelpFormatter f = new HelpFormatter();
			f.printHelp("MtServerBoostrap -p port_number [options]", opts);
			System.exit(1);
		}
		
		MtRpcServer server = new MtRpcServer(boostrap.bindingAddress, boostrap.port, boostrap.tcpBackLog, boostrap.maxThread);
		try{
			server.start();
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}finally{
			server.shutdown();
		}
	}



	public int getMaxThread() {
		return maxThread;
	}



	public void setMaxThread(int maxThread) {
		this.maxThread = maxThread;
	}



	public String getBindingAddress() {
		return bindingAddress;
	}



	public void setBindingAddress(String bindingAddress) {
		this.bindingAddress = bindingAddress;
	}



	public int getPort() {
		return port;
	}



	public void setPort(int port) {
		this.port = port;
	}

	

	public boolean isShowHelp() {
		return showHelp;
	}



	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
	}



	public int getTcpBackLog() {
		return tcpBackLog;
	}



	public void setTcpBackLog(int tcpBackLog) {
		this.tcpBackLog = tcpBackLog;
	}



	public boolean isSsl() {
		return ssl;
	}



	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}
	
	
	

}
