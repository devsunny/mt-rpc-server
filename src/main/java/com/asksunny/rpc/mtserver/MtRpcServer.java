package com.asksunny.rpc.mtserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * keytool -genkey -keystore MtRpcServerKeystore -keyalg RSA
 * java -Djavax.net.ssl.keyStore=MtRpcServerKeystore -Djavax.net.ssl.keyStorePassword=changeit com.asksunny.rpc.mtserver.MtRpcServer
 * java -Djavax.net.ssl.keyStore=MtRpcServerKeystore -Djavax.net.ssl.trustStorePassword=changeit com.asksunny.rpc.mtserver.MtRpcClient
 * java -Djavax.net.ssl.keyStore=MtRpcServerKeystore -Djavax.net.ssl.trustStorePassword=changeit com.asksunny.rpc.mtserver.MtRpcClientConsole
 *  
 * @author SunnyLiu
 *
 */
public class MtRpcServer {
	
	final static Logger log = LoggerFactory.getLogger(MtRpcServer.class);
	ExecutorService pooledExecutor = null;
	String bindingAddress; 
	int port; 
	int tcpBackLog;
	boolean ssl;
	AtomicBoolean stop = new AtomicBoolean(false);
	
	
	public MtRpcServer(String bindingAddress, int port, int tcpBackLog, int threadPoolSize)
	{
		int max_t = (threadPoolSize<10)?10:threadPoolSize;
		pooledExecutor = java.util.concurrent.Executors.newFixedThreadPool(max_t);
		this.bindingAddress = bindingAddress;
		this.port = port;
		this.tcpBackLog = tcpBackLog;		
		final ExecutorService usedPool = pooledExecutor;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {			
			public void run() {				
				usedPool.shutdownNow();
			}
		}));
	}

	public MtRpcServer(String bindingAddress, int port, int tcpBackLog) {		
		this(bindingAddress,  port,  tcpBackLog, 10);
	}
	
	public MtRpcServer(int port) {		
		this(null,  port,  200, 10);
	}
	
	
	public void start() throws IOException
	{
		ServerSocket  server = createServerSocket();
		server.setReuseAddress(true);			
		while(!stop.get())
		{
			Socket client = server.accept();
			if(log.isInfoEnabled()) log.info("Acception connection from {}", client.getInetAddress());			
			pooledExecutor.submit(new RpcWorker(this, client));			
		}
		
	}
	
	
	
	protected ServerSocket createServerSocket() throws IOException
	{
		 
		SocketAddress addr =(this.bindingAddress!=null)? new InetSocketAddress(this.bindingAddress, this.port): new InetSocketAddress(this.port);
		if(this.ssl){
			SSLServerSocketFactory sslserversocketfactory =
                 (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			SSLServerSocket sslserversocket =
                 (SSLServerSocket) sslserversocketfactory.createServerSocket();
			sslserversocket.bind(addr, this.tcpBackLog);
			return sslserversocket;
		}else{
			ServerSocketFactory serversocketfactory =
	                 (ServerSocketFactory) ServerSocketFactory.getDefault();
				ServerSocket serversocket =
	                 (ServerSocket) serversocketfactory.createServerSocket();
				serversocket.bind(addr, this.tcpBackLog);
				return serversocket;
		}
         
	}
	
		
	
	
	public void shutdown()
	{
		pooledExecutor.shutdown();
	}

	public ExecutorService getPooledExecutor() {
		return pooledExecutor;
	}

	public void setPooledExecutor(ExecutorService pooledExecutor) {
		this.pooledExecutor = pooledExecutor;
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
