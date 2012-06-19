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

package org.openecard.client.common.apdu;

import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.tlv.TLV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * GENERAL AUTHENTICATION Command
 * See ISO/IEC 7816-4 Section 7.5.2
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GeneralAuthenticate extends CardCommandAPDU {

    private static final Logger _logger = LoggerFactory.getLogger(GeneralAuthenticate.class);

    /**
     * GENERAL AUTHENTICATION command instruction byte
     */
    private static final byte GENERAL_AUTHENTICATION_INS = (byte) 0x86;

    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x00 0x86 0x00 0x00 0x02 0x7C 0x00 0x00
     */
    public GeneralAuthenticate() {
	super(x00, GENERAL_AUTHENTICATION_INS, x00, x00);
	setLC((byte) 0x02);
	setData(new byte[]{(byte) 0x7C, x00});
	setLE(x00);
    }

    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x10 0x86 0x00 0x00 LC 0x7C DATA 0x00
     *
     * @param data Data
     */
    public GeneralAuthenticate(byte[] data) {
	super(x00, GENERAL_AUTHENTICATION_INS, x00, x00);
	setData(data);
	setLE(x00);
    }

    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x00 0x86 0x00 0x00 LC 0x7C DATA 0x00
     * Tag should be one of:
     * '80' Witness
     * '81' Challenge
     * '82' Response
     * '83' Committed challenge
     * '84' Authentication code
     * '85' Exponential
     * 'A0' Identification data template
     *
     * @param tag Authentication data tag. 0x7C is omitted!
     * @param authData Authentication data
     */
    public GeneralAuthenticate(byte tag, byte[] authData) {
	super(x00, GENERAL_AUTHENTICATION_INS, x00, x00);

	try {
	    TLV tag7c = new TLV();
	    TLV tagData = new TLV();

	    tag7c.setTagNumWithClass((byte) 0x7C);
	    tag7c.setChild(tagData);
	    tagData.setTagNumWithClass((byte) tag);
	    tagData.setValue(authData);

	    setData(tag7c.toBER());
	} catch (Exception ex) {
	    _logger.error(ex.getMessage(), ex);
	}

	setLE(x00);
    }

}
