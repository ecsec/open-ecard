package de.ecsec.ecard.client.event;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class EventHandler implements Runnable {

    private Event event;
    private Object eventData;

    public EventHandler(Event event, Object eventData) {
	this.event = event;
	this.eventData = eventData;
    }

    @Override
    public void run() {
	event.notify(eventData);
    }

}
