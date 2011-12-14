package org.openecard.client.common;

import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventManager;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.openecard.ws.IFD;
import org.openecard.ws.SAL;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class ClientEnv implements Environment {

    private IFD ifd;
    private SAL sal;
    private EventManager manager;
    private Dispatcher dispatcher;
    private Map<String, Object> genericComponents;

    public ClientEnv() {
        genericComponents = new ConcurrentSkipListMap<String, Object>();
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
    public void setSAL(SAL sal) {
        this.sal = sal;
    }

    @Override
    public SAL getSAL() {
        return sal;
    }

    @Override
    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public void setGenericComponent(String id, Object component) {
        genericComponents.put(id, component);
    }

    @Override
    public Object getGenericComponent(String id) {
        return genericComponents.get(id);
    }
    
}
