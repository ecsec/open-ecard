package de.ecsec.ecard.client.event;

import de.ecsec.core.common.enums.EventType;
import de.ecsec.core.common.interfaces.EventCallback;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class Event {

    private final EventType type;
    private List<EventCallback> callbacks;

    public Event(EventType type) {
	this.type = type;
	this.callbacks = new CopyOnWriteArrayList<EventCallback>();
    }

    public void addListener(EventCallback callback) {
	callbacks.add(callback);
    }

    public void removeListener(EventCallback callback) {
	callbacks.remove(callback);
    }

    public void notify(Object eventData) {
	EventCallback callback;
	for (Iterator<EventCallback> iter = callbacks.iterator(); iter.hasNext(); ) {
	    callback = iter.next();
	    callback.signalEvent(type, eventData);
	}
    }

    public EventType getType() {
	return type;
    }

    public String getName() {
	return type.name();
    }

}
