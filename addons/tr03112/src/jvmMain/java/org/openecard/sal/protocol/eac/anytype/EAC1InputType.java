/****************************************************************************
 * Copyright (C) 2012-2014 HS Coburg.
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
import java.util.ArrayList;
import javax.annotation.Nullable;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.crypto.common.asn1.cvc.CHAT;


/**
 * Implements the EAC1InputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.5.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class EAC1InputType {

    private static final Logger logger = LoggerFactory.getLogger(EAC1InputType.class);

    public static final String CERTIFICATE = "Certificate";
    public static final String CERTIFICATE_DESCRIPTION = "CertificateDescription";
    public static final String PROVIDER_INFO = "ProviderInfo";
    public static final String REQUIRED_CHAT = "RequiredCHAT";
    public static final String OPTIONAL_CHAT = "OptionalCHAT";
    public static final String AUTHENTICATED_AUXILIARY_DATA = "AuthenticatedAuxiliaryData";
    public static final String TRANSACTION_INFO = "TransactionInfo";

    private final AuthDataMap authMap;
    private final ArrayList<CardVerifiableCertificate> certificates;
    private final byte[] certificateDescription;
    private final byte[] providerInfo;
    private final byte[] requiredCHAT;
    private final byte[] optionalCHAT;
    private final byte[] authenticatedAuxiliaryData;
    private final String transactionInfo;

    /**
     * Creates a new EAC1InputType.
     *
     * @param baseType DIDAuthenticationDataType
     * @throws Exception Thrown in cause the type iss errornous.
     */
    public EAC1InputType(DIDAuthenticationDataType baseType) throws Exception {
	parseCertificateDescriptionElement(baseType);
	authMap = new AuthDataMap(baseType);

	certificates = new ArrayList<>();
	for (Element element : baseType.getAny()) {
	    if (element.getLocalName().equals(CERTIFICATE)) {
		byte[] value = StringUtils.toByteArray(element.getTextContent());
		CardVerifiableCertificate cvc = new CardVerifiableCertificate(value);
		certificates.add(cvc);
	    }
	}
	certificateDescription = authMap.getContentAsBytes(CERTIFICATE_DESCRIPTION);

	providerInfo = authMap.getContentAsBytes(PROVIDER_INFO);

	byte[] requiredCHATtmp = authMap.getContentAsBytes(REQUIRED_CHAT);
	byte[] optionalCHATtmp = authMap.getContentAsBytes(OPTIONAL_CHAT);
	// HACK: this is only done because some eID Server vendors send raw CHAT values
	// if not present use empty CHAT, so everything can be deselected
	if (requiredCHATtmp == null) {
	    requiredCHATtmp = new CHAT().toByteArray();
	} else {
	    requiredCHATtmp = fixChatValue(requiredCHATtmp);
	}
	// if not present, use terminal CHAT as optional
	if (optionalCHATtmp == null) {
	    CardVerifiableCertificateChain certChain = new CardVerifiableCertificateChain(certificates);
	    CardVerifiableCertificate terminalCert = certChain.getTerminalCertificate();
	    optionalCHATtmp = terminalCert.getCHAT().toByteArray();
	} else {
	    optionalCHATtmp = fixChatValue(optionalCHATtmp);
	}
	requiredCHAT = requiredCHATtmp;
	optionalCHAT = optionalCHATtmp;

	authenticatedAuxiliaryData = authMap.getContentAsBytes(AUTHENTICATED_AUXILIARY_DATA);

	transactionInfo = authMap.getContentAsString(TRANSACTION_INFO);
    }

    /**
     * Returns the set of certificates.
     *
     * @return Certificates
     */
    public ArrayList<CardVerifiableCertificate> getCertificates() {
	return certificates;
    }

    /**
     * Returns the certificate description.
     *
     * @return Certificate description
     * @deprecated See BSI TR-03112-7 Sec. 3.6.4.1
     */
    @Deprecated
    public byte[] getCertificateDescription() {
	return certificateDescription;
    }

    /**
     * Gets the provider info.
     *
     * @return Provider info.
     */
    public byte[] getProviderInfo() {
	return providerInfo;
    }

    /**
     * Returns the required CHAT.
     *
     * @return Required CHAT
     */
    public byte[] getRequiredCHAT() {
	return requiredCHAT;
    }

    /**
     * Returns the optional CHAT.
     *
     * @return Optional CHAT
     */
    public byte[] getOptionalCHAT() {
	return optionalCHAT;
    }

    /**
     * Returns the AuthenticatedAuxiliaryData.
     *
     * @return AuthenticatedAuxiliaryData
     */
    public byte[] getAuthenticatedAuxiliaryData() {
	return authenticatedAuxiliaryData;
    }

    /**
     * Gets the value from the TransactionInfo element.
     *
     * @return Value in the TransactionInfo element, or {@code null} if none is present.
     */
    @Nullable
    public String getTransactionInfo() {
	return transactionInfo;
    }

    /**
     * Returns a new EAC1OutputType.
     *
     * @return EAC1OutputType
     */
    public EAC1OutputType getOutputType() {
	return new EAC1OutputType(authMap);
    }


    /**
     * Adds ASN1 Structure to incomplete CHAT values.
     * Some eID servers only send the CHAT value itself, but there must an OID and a surrounding ASN1 structure. This
     * function completes the CHAT value with the AuthenticationTerminal OID.
     *
     * @param chat CHAT value, possibly without ASN1 structure.
     * @return  CHAT value with ASN1 structure.
     */
    private static byte[] fixChatValue(byte[] chat) {
	if (chat.length == 5) {
	    logger.warn("Correcting invalid CHAT value '{}'.", ByteUtils.toHexString(chat));
	    String asn1Prefix = "7F4C12060904007F0007030102025305";
	    byte[] prefixBytes = StringUtils.toByteArray(asn1Prefix);
	    byte[] result = ByteUtils.concatenate(prefixBytes, chat);
	    return result;
	} else {
	    return chat;
	}
    }

    /**
     * Parse the occurrences of the CertificateDescription.
     *
     * Note: The current schema says there could be several CertificateDescriptions. But the Technical Guideline says
     * there have to be  one single CertificateDescription object.
     *
     * @param baseType
     * @throws ElementParsingException
     */
    private void parseCertificateDescriptionElement(DIDAuthenticationDataType baseType) throws ElementParsingException {
	int counter = 0;
	for (Element element : baseType.getAny()) {
	    if (element.getLocalName().equals(CERTIFICATE_DESCRIPTION)) {
		counter++;
		if (counter > 1) {
		    throw new ElementParsingException(INVALID_CERT);
		}
	    }
	}

	if (counter == 0) {
	    throw new ElementParsingException(INVALID_CERT);
	}
    }

}
