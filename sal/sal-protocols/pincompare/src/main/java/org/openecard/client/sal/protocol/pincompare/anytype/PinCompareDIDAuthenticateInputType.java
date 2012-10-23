/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.sal.protocol.pincompare.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.client.common.anytype.AuthDataMap;


/**
 * [TR-03112-7] This type specifies the structure of the
 * DIDAuthenticationDataType for the PIN Compare protocol when DIDAuthenticate
 * is called.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PinCompareDIDAuthenticateInputType {

    /**
     * MAY contain the value of the PIN. If this element is missing, it is input
     * at the terminal.
     */
    private String pin = null;
    private final AuthDataMap authMap;

    /**
     * 
     * @param baseType
     *            a DIDAuthenticationDataType of type
     *            PinCompareDIDAuthenticateInputType
     * @throws ParserConfigurationException
     *             to indicate a XML configuration error
     */
    public PinCompareDIDAuthenticateInputType(DIDAuthenticationDataType baseType) throws ParserConfigurationException {
	authMap = new AuthDataMap(baseType);
	// Optional contents
	pin = authMap.getContentAsString("Pin");
    }

    /**
     * 
     * @return the pin or null if not present
     */
    public String getPin() {
	return pin;
    }

    /**
     * 
     * @return the corresponding PinCompareDIDAuthenticateOutputType for this
     *         PinCompareDIDAuthenticateInputType
     */
    public PinCompareDIDAuthenticateOutputType getOutputType() {
	return new PinCompareDIDAuthenticateOutputType(authMap);
    }

}
