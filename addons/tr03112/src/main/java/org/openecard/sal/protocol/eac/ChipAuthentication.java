/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.sal.protocol.eac;

import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import java.lang.reflect.InvocationTargetException;
import org.openecard.common.apdu.GeneralAuthenticate;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.apdu.utils.CardUtils;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.sal.protocol.exception.ProtocolException;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.ShortUtils;
import org.openecard.sal.protocol.eac.apdu.MSESetATCA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the Chip Authentication protocol.
 * See BSI-TR-03110, version 2.10, part 2, Section B.3.3.
 * See BSI-TR-03110, version 2.10, part 3, Section B.2.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ChipAuthentication {

    private static final Logger logger = LoggerFactory.getLogger(ChipAuthentication.class);

    private final Dispatcher dispatcher;
    private final byte[] slotHandle;

    /**
     * Creates a new Chip Authentication.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     */
    public ChipAuthentication(Dispatcher dispatcher, byte[] slotHandle) {
	this.dispatcher = dispatcher;
	this.slotHandle = slotHandle;
    }

    /**
     * Initializes the Chip Authentication protocol.
     * Sends an MSE:Set AT APDU. (Protocol step 1)
     * See BSI-TR-03110, version 2.10, part 3, B.11.1.
     *
     * @param oID Chip Authentication object identifier
     * @param keyID Key identifier
     * @throws ProtocolException
     */
    public void mseSetAT(byte[] oID, byte[] keyID) throws ProtocolException {
	try {
	    CardCommandAPDU mseSetAT = new MSESetATCA(oID, keyID);
	    mseSetAT.transmit(dispatcher, slotHandle);
	} catch (APDUException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Performs a General Authenticate.
     * Sends an General Authenticate APDU. (Protocol step 2)
     * See BSI-TR-03110, version 2.10, part 3, B.11.2.
     *
     * @param key Ephemeral Public Key
     * @return Response APDU
     * @throws ProtocolException
     */
    public byte[] generalAuthenticate(byte[] key) throws ProtocolException {
	try {
	    if (key[0] != (byte) 0x04) {
		key = ByteUtils.concatenate((byte) 0x04, key);
	    }
	    CardCommandAPDU generalAuthenticate = new GeneralAuthenticate((byte) 0x80, key);
	    CardResponseAPDU response = generalAuthenticate.transmit(dispatcher, slotHandle);

	    return response.getData();
	} catch (APDUException e) {
	    throw new ProtocolException(e.getResult());
	}
    }

    /**
     * Reads the EFCardSecurity from the card.
     *
     * @return EFCardSecurtiy
     * @throws ProtocolException Thrown in case there is a problem reading the file.
     */
    public byte[] readEFCardSecurity() throws ProtocolException {
	try {
	    byte[] file = ShortUtils.toByteArray(EACConstants.EF_CARDSECURITY_FID);
	    CardResponseAPDU resp = CardUtils.selectFileWithOptions(dispatcher, slotHandle, file, null,
		    CardUtils.FCP_RESPONSE_DATA);
	    FCP efCardSecurityFCP = new FCP(TLV.fromBER(resp.getData()));
	    byte[] efCardSecurity = CardUtils.readFile(efCardSecurityFCP, dispatcher, slotHandle);
	    return efCardSecurity;
	} catch (APDUException ex) {
	    throw new ProtocolException(ex.getResult());
	} catch (TLVException ex) {
	    throw new ProtocolException("Failed to parse FCP.", ex);
	}
    }

    /**
     * Destroys a previously established PACE channel.
     */
    public void destroySecureChannel() {
	try {
	    DestroyChannel destroyChannel = new DestroyChannel();
	    destroyChannel.setSlotHandle(slotHandle);
	    dispatcher.deliver(destroyChannel);
	} catch (InvocationTargetException | DispatcherException ex) {
	    // ignore and hope for the best
	    logger.warn("Failed to disable secure messaging channel.");
	}
    }

}
