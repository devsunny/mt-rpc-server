package com.asksunny.rpc.mtserver.peer;

public class PeerEvent {

	
	int eventId;
	String eventMessage;
	
	
	public PeerEvent() {
		
	}


	public int getEventId() {
		return eventId;
	}


	public void setEventId(int eventId) {
		this.eventId = eventId;
	}


	public String getEventMessage() {
		return eventMessage;
	}


	public void setEventMessage(String eventMessage) {
		this.eventMessage = eventMessage;
	}
	
	
	

}
