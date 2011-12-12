package org.openecard.client.common.interfaces;

/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public interface AsyncTransportCallback {
    
    public Object receive(Object message);
}
