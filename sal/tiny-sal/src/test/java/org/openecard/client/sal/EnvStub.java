package org.openecard.client.sal;

import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventManager;
import org.openecard.client.common.interfaces.Transport;
import org.openecard.ws.IFD;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class EnvStub implements Environment {

    private IFD ifd;
    private EventManager manager;
    private Map<String, Transport> transports;
    
    public EnvStub() {
        transports = new ConcurrentSkipListMap<String, Transport>();
    }
    
    public EnvStub(IFD ifd, EventManager manager) {
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
