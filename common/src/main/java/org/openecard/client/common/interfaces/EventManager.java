package org.openecard.client.common.interfaces;

import org.openecard.client.common.enums.EventType;
import java.util.List;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface EventManager {
    
    public Object initialize();
    
    public void terminate();
    
    public void register(EventCallback callback, EventFilter filter);
    
    public void register(EventCallback callback, EventType type);
    
    public void register(EventCallback callback, List<EventType> types);
    
    public void registerAllEvents(EventCallback callback);
    
    public void unregister(EventCallback callback);
}
