package de.ecsec.ecard.client.event;

import java.util.Date;

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
        System.out.println("EventHandler :: run (" + Thread.currentThread().getName() + ")");
        System.out.println("EventHandler :: start @ " + new Date(System.currentTimeMillis()));
        event.notify(eventData);
        System.out.println("EventHandler :: end @ " + new Date(System.currentTimeMillis()));
        System.out.println("EventHandler :: run (" + Thread.currentThread().getName() + ")");
    }
    
    
}
