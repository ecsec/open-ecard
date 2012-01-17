package org.openecard.client.android;


import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;

import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;

public class ClientEventCallBack implements EventCallback {

	public void signalEvent(EventType eventType, Object eventData) {
		  System.out.println("signalEvent: Type: " + eventType.toString());
		  if(eventData instanceof ConnectionHandleType){
			  ConnectionHandleType ch = (ConnectionHandleType) eventData;
			 System.out.println("Card recognized as: " + ch.getRecognitionInfo().getCardType());
		  }
	}
}
