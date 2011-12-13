package org.openecard.client.ifd.proto.pace;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.ifd.ProtocolFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PaceProtocolFactory implements ProtocolFactory {

    @Override
    public String getProtocol() {
        return ECardConstants.Protocol.PACE;
    }

    @Override
    public Protocol createInstance() {
        return new PaceProtocol();
    }

}
