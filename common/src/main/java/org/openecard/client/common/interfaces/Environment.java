package org.openecard.client.common.interfaces;

import java.util.Map;
import org.openecard.ws.IFD;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface Environment {
    
    public void setIFD(IFD ifd);
    
    public IFD getIFD();

    public void setEventManager(EventManager manager);
    
    public EventManager getEventManager();
    
    public void addTransport(String id, Transport transport);
    
    public Transport getTransport(String id);
    
    public Map<String, Transport> getAllTransports();

}
