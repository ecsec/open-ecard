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

package org.openecard.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.anytype.AuthDataMap;


/**
 * Implements the PACEInputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.3.5.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PACEInputType {

    public static final String PIN_ID = "PinID";
    public static final String CHAT = "CHAT";
    public static final String PIN = "PIN";
    public static final String CERTIFICATE_DESCRIPTION = "CertificateDescription";
    //
    private final AuthDataMap authMap;
    private final String pin;
    private final byte pinID;
    private final byte[] chat;
    private final byte[] certDesc;

    /**
     * Creates a new PACEInputType.
     *
     * @param baseType DIDAuthenticationDataType
     * @throws ParserConfigurationException
     */
    public PACEInputType(DIDAuthenticationDataType baseType) throws ParserConfigurationException {
	authMap = new AuthDataMap(baseType);

	pinID = authMap.getContentAsBytes(PIN_ID)[0];
	// optional elements
	chat = authMap.getContentAsBytes(CHAT);
	pin = authMap.getContentAsString(PIN);
	certDesc = authMap.getContentAsBytes(CERTIFICATE_DESCRIPTION);
    }

    /**
     * Returns the PIN ID.
     *
     * @return PIN ID
     */
    public byte getPINID() {
	return pinID;
    }

    /**
     * Returns the CHAT.
     *
     * @return CHAT
     */
    public byte[] getCHAT() {
	return chat;
    }

    /**
     * Returns the PIN.
     *
     * @return PIN
     */
    public String getPIN() {
	return pin;
    }

    /**
     * Returns the certificate description.
     *
     * @return Certificate description
     */
    public byte[] getCertificateDescription() {
	return certDesc;
    }

    /**
     * Returns a PACEOutputType based on the PACEInputType.
     *
     * @return PACEOutputType
     */
    public PACEOutputType getOutputType() {
	return new PACEOutputType(authMap);
    }

}
