package org.openecard.client.common.ifd;

import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import org.openecard.ws.IFD;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface Protocol {

    /**
     * Perform protocol and thereby set up a secure messaging channel.
     *
     * @param req Request data needed for the protocol
     * @param ifd IFD instance to perform commands on the terminal
     * @param vTerm Virtual terminal which can be used to get secrets (e.g. PIN) from the user
     * @param slotHandle SlotHandle to identify card and terminal
     * @return Protocol response data
     */
    public EstablishChannelResponse establish(EstablishChannel req, IFD ifd, VirtualTerminal vTerm, byte[] slotHandle);

    /**
     * Filter function to perform secure messaging after the protocol has been enstablished.<br/>
     * Apply secure messaging encryption to APDU.
     *
     * @param commandApdu Command APDU which should be encrypted
     * @return Command APDU which is encrypted
     */
    public byte[] applySM(byte[] commandApdu);

    /**
     * Filter function to perform secure messaging after the protocol has been enstablished.<br/>
     * Remove secure messaging encryption from APDU.
     *
     * @param responseApdu Response APDU which should be decrypted
     * @return Response APDU which is encrypted
     */
    public byte[] removeSM(byte[] responseApdu);

}
