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

package org.openecard.sal.protocol.pincompare.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.anytype.AuthDataMap;


/**
 * Implements the PINCompareDIDAuthenticateInputType.
 * See TR-03112, version 1.1.2, part 7, section 4.1.5.
 *
 * @author Dirk Petrautzki
 */
public class PINCompareDIDAuthenticateInputType {

    private final AuthDataMap authMap;
    private String pin;

    /**
     * Creates a new PINCompareDIDAuthenticateInputType.
     *
     * @param data DIDAuthenticationDataType
     * @throws ParserConfigurationException
     */
    public PINCompareDIDAuthenticateInputType(DIDAuthenticationDataType data) throws ParserConfigurationException {
	authMap = new AuthDataMap(data);
	// Optional contents
	pin = authMap.getContentAsString("Pin");
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
     * Returns a new PINCompareDIDAuthenticateOutputType.
     *
     * @return PINCompareDIDAuthenticateOutputType
     */
    public PINCompareDIDAuthenticateOutputType getOutputType() {
	return new PINCompareDIDAuthenticateOutputType(authMap);
    }

}
