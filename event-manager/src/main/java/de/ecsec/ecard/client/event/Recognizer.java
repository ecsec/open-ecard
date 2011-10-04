package de.ecsec.ecard.client.event;

import de.ecsec.core.common.enums.EventType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Recognizer implements Runnable {

    private final EventManager manager;
    private final EventType[] events;
    private final String ifdName;
    private final SlotStatusType status;

    public Recognizer(EventManager manager, String ifdName, SlotStatusType status, EventType... events) {
	this.manager = manager;
	this.events = events;
	this.ifdName = ifdName;
	this.status = status;
    }

    @Override
    public void run() {
	if (events.length > 0) {
	    ConnectionHandleType conHandle = manager.recognizeSlot(ifdName, status, false);
	    ConnectionHandleType conHandleRecog = null;
	    for (EventType type : events) {
		// let's hope, that CARD_RECOGNIZED comes last
		if (type.equals(EventType.CARD_RECOGNIZED)) {
		    if (conHandleRecog == null) {
			conHandleRecog = manager.recognizeSlot(ifdName, status, true);
			manager.notify(type, conHandleRecog);
		    }
		} else {
                    manager.notify(type, conHandle);
                }
	    }
	}
    }

}
