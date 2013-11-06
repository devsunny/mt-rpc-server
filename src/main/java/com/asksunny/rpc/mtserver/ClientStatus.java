package com.asksunny.rpc.mtserver;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientStatus {
	
	SocketAddress localSocketAddress;
	InetAddress clientAddress;
	long connectedTime  = System.nanoTime();
	AtomicLong bytesIn = new AtomicLong(0L);
	AtomicLong bytesOut = new AtomicLong(0L);
	
	
	public ClientStatus(Socket clientSocket)
	{
		this.clientAddress = clientSocket.getInetAddress();
		this.localSocketAddress = clientSocket.getLocalSocketAddress();		
	}
	
	public long bytesSent(long bytes)
	{
		return bytesOut.addAndGet(bytes);
	}
	
	public long bytesReceived(long bytes)
	{
		return bytesIn.addAndGet(bytes);
	}
	
	
	public long getBytesOut()
	{
		return bytesOut.get();
	}
		
	public long getBytesIn()
	{
		return bytesIn.get();
	}
		
	
	public long getConnectedDuration(TimeUnit tu)
	{		
		if(tu == TimeUnit.NANOSECONDS){
			return System.nanoTime() - connectedTime;
		}else if(tu == TimeUnit.MILLISECONDS){
			return (System.nanoTime() - connectedTime)/1000000L;
		}else if(tu == TimeUnit.SECONDS){
			return (System.nanoTime() - connectedTime)/1000000000L;
		}else if(tu == TimeUnit.MINUTES){
			return (System.nanoTime() - connectedTime)/(1000000000L*60L);
		}else if(tu == TimeUnit.HOURS){
			return (System.nanoTime() - connectedTime)/(1000000000L*60L*60L);
		}else if(tu == TimeUnit.DAYS){
			return (System.nanoTime() - connectedTime)/(1000000000L*60L*60L*24L);
		}else{
			return (System.nanoTime() - connectedTime)/(1000000000L*60L);
		}		
	}
	
	
	
	public long getConnectedTime() {
		return connectedTime;
	}
	public void setConnectedTime(long connectedTime) {
		this.connectedTime = connectedTime;
	}
	public SocketAddress getLocalSocketAddress() {
		return localSocketAddress;
	}
	public void setLocalSocketAddress(SocketAddress localSocketAddress) {
		this.localSocketAddress = localSocketAddress;
	}
	public InetAddress getClientAddress() {
		return clientAddress;
	}
	public void setClientAddress(InetAddress clientAddress) {
		this.clientAddress = clientAddress;
	}
	
	
}
