package org.openecard.client.common.interfaces;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface Transport {
    
    public Object send(Object message);
}
