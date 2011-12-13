package org.openecard.client.ifd.proto.pace;

import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.gui.UserConsent;
import org.openecard.ws.IFD;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PaceProtocol implements Protocol {

    @Override
    public EstablishChannelResponse establish(EstablishChannel req, IFD ifd, UserConsent gui) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] applySM(byte[] commandApdu) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] removeSM(byte[] responseApdu) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
