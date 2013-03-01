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

package org.openecard.ifd.scio.reader;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.util.ShortUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EstablishPACERequest {

    private byte passwordType;
    private byte chatLength = 0;
    private byte[] chat;
    private byte passwordLength = 0;
    private byte[] password;
    private short certDescLength = 0;
    private byte[] certDesc;

    public EstablishPACERequest(byte passwordType, byte[] chat, byte[] password, byte[] certDesc) {
	this.passwordType = passwordType;
	if (chat != null) {
	    this.chatLength = (byte) chat.length;
	    this.chat = chat;
	}
	if (password != null) {
	    this.passwordLength = (byte) password.length;
	    this.password = password;
	}
	if (certDesc != null) {
	    this.certDescLength = (short) certDesc.length;
	    this.certDesc = certDesc;
	}
    }

    public boolean isSupportedType(List<PACECapabilities.PACECapability> capabilities) {
	// perform sanity check of the request according to BSI-TR-03119_V1
	// Für eine Durchführung von PACE in der Rolle
	// + eines nicht-authentisierten Terminals (Capability PACE) ist nur die Position 1 vorhanden
	// + in der Rolle Authentisierungsterminal (Capability eID) sind alle Positionen anzugeben
	// + in der Rolle Signaturterminal (Capability QES) sind die Positionen 1-3 und ggfs. 4-5 (für
	//     Passwort CAN, sofern dieses nicht am Leser eingegeben wird) anzugeben.
	if (chat == null && certDesc == null) {
	    return capabilities.contains(PACECapabilities.PACECapability.GenericPACE);
	} else if (chat != null && certDesc != null) {
	    return capabilities.contains(PACECapabilities.PACECapability.GermanEID);
	} else if (chat != null && certDesc == null) {
	    return capabilities.contains(PACECapabilities.PACECapability.QES);
	}
	return false;
    }

    public byte[] toBytes() {
	ByteArrayOutputStream o = new ByteArrayOutputStream();
	o.write(passwordType);
	// the following elements are only present if PACE is followed by TA v2
	if (chatLength > 0) {
	    o.write(chatLength);
	    if (chatLength > 0) {
		o.write(chat, 0, chat.length);
	    }
	    o.write(passwordLength);
	    if (passwordLength > 0) {
		o.write(password, 0, password.length);
	    }
	    // optional application specific data (only certs possible at the moment)
	    if (certDescLength > 0) {
		// write data length
		byte[] dataLength_bytes = ShortUtils.toByteArray(certDescLength);
		for (int i = dataLength_bytes.length - 1; i >= 0; i--) {
		    o.write(dataLength_bytes[i]);
		}
		// write missing bytes to length field
		for (int i = dataLength_bytes.length; i < 2; i++) {
		    o.write(0);
		}
		// write data
		o.write(certDesc, 0, certDesc.length);
	    }
	}

	return o.toByteArray();
    }

}
