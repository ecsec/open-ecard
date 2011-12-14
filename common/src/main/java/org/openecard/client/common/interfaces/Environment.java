package org.openecard.client.common.interfaces;

import org.openecard.ws.IFD;
import org.openecard.ws.SAL;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface Environment {

    public void setIFD(IFD ifd);
    public IFD getIFD();

    public void setSAL(SAL sal);
    public SAL getSAL();

    public void setEventManager(EventManager manager);
    public EventManager getEventManager();

    public void setDispatcher(Dispatcher dispatcher);
    public Dispatcher getDispatcher();

    public void setGenericComponent(String id, Object component);
    public Object getGenericComponent(String id);

}
