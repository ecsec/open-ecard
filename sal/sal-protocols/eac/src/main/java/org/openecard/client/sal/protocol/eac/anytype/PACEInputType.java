/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.client.common.sal.anytype.AuthDataMap;


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
