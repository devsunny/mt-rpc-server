package com.asksunny.rpc.mtserver.peer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusterPeerManager {

	ConcurrentMap<String, ClusterPeer> peers = new ConcurrentHashMap<String, ClusterPeer>();
	
	public ClusterPeerManager() {
		
	}
	
	public ClusterPeerManager joinMash(ClusterPeer peer)
	{
		peers.put(peer.getKey(), peer);
		return this;
	}
	
	
	public ClusterPeerManager remove(ClusterPeer peer)
	{
		peers.remove(peer.getKey(), peer);
		return this;
	}
	
	
	public ClusterPeerManager broadcast(PeerEvent event)
	{
		
		
		return this;
	}
	
	public ClusterPeerManager unicast(ClusterPeer peer, PeerEvent event)
	{
		
		
		return this;
	}
	
	public ClusterPeerManager unicast(String  host, int port, PeerEvent event)
	{
		
		
		return this;
	}
	
}
