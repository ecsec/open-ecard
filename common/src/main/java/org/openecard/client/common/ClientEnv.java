package org.openecard.client.common;

import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventManager;
import org.openecard.client.common.interfaces.Transport;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.openecard.ws.IFD;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class ClientEnv implements Environment {

    private IFD ifd;
    private EventManager manager;
    private Map<String, Transport> transports;
    
    public ClientEnv() {
        transports = new ConcurrentSkipListMap<String, Transport>();
    }
    
    public ClientEnv(IFD ifd, EventManager manager) {
        this.ifd = ifd;
        this.manager = manager;
        transports = new ConcurrentSkipListMap<String, Transport>();
    }

    @Override
    public void setIFD(IFD ifd) {
        this.ifd = ifd;
    }

    @Override
    public IFD getIFD() {
        return ifd;
    }

    @Override
    public void setEventManager(EventManager manager) {
        this.manager = manager;
    }

    @Override
    public EventManager getEventManager() {
        return manager;
    }

    @Override
    public void addTransport(String id, Transport transport) {
        transports.put(id, transport);
    }

    @Override
    public Transport getTransport(String id) {
        return transports.get(id);
    }

    @Override
    public Map<String, Transport> getAllTransports() {
        return transports;
    }
    
}
