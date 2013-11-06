package com.asksunny.rpc.mtserver;

import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerStateMonitor 
{
	ConcurrentHashMap<Socket, ClientStatus> activeClients = null;
	static ServerStateMonitor instance = new ServerStateMonitor();
	
		
	public static void registerNewClient(Socket clientSocket)
	{
		instance.activeClients.put(clientSocket, new ClientStatus(clientSocket));
	}
	
	
	public static void unregisterClient(Socket clientSocket)
	{
		instance.activeClients.remove(clientSocket);
	}
	
	
	public static int getActionConnections()
	{
		return instance.activeClients.size();
	}
	
	public static Collection<ClientStatus> getClientStatus()
	{
		return instance.activeClients.values();
	}
	
	
	private ServerStateMonitor(){
		activeClients = new ConcurrentHashMap<Socket, ClientStatus>();
	};
}
