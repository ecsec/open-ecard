package org.openecard.client.common.sal;

import org.openecard.client.gui.UserConsent;
import org.openecard.ws.IFD;
import org.openecard.ws.SAL;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface ProtocolFactory {

    /**
     * Get URI of the protocol the instances created by this factory supports.
     *
     * @return URI of the supported protocol
     */
    public String getProtocol();

    /**
     * Create instance of the protocol.
     *
     * @return instance of the protocol which can be used for one connection
     */
    public Protocol createInstance(SAL sal, IFD ifd, UserConsent gui);

}
