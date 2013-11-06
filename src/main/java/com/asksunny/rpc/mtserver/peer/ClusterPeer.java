package com.asksunny.rpc.mtserver.peer;


public class ClusterPeer {

	
	String host;
	int port;
	double weight;
	long startedTime;
	
	
	
	public String getKey()
	{
		return String.format("tcp://%s:%d", host, port);
	}
	
	
	public ClusterPeer() {
		
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public double getWeight() {
		return weight;
	}


	public void setWeight(double weight) {
		this.weight = weight;
	}


	public long getStartedTime() {
		return startedTime;
	}


	public void setStartedTime(long startedTime) {
		this.startedTime = startedTime;
	}
	
	

}
