/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import java.util.Map;
import org.openecard.common.sal.protocol.exception.ProtocolException;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.util.IntegerUtils;
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.crypto.common.asn1.eac.AuthenticatedAuxiliaryData;
import org.openecard.crypto.common.asn1.eac.CADomainParameter;
import org.openecard.crypto.common.asn1.eac.CASecurityInfos;
import org.openecard.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.crypto.common.asn1.eac.ef.EFCardAccess;
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.openecard.sal.protocol.eac.anytype.EAC2OutputType;
import org.openecard.sal.protocol.eac.crypto.CAKey;


/**
 * Helper class combining TA and CA
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class AuthenticationHelper {

    private final TerminalAuthentication ta;
    private final ChipAuthentication ca;

    public AuthenticationHelper(TerminalAuthentication ta, ChipAuthentication ca) {
	this.ta = ta;
	this.ca = ca;
    }

    public EAC2OutputType performAuth(EAC2OutputType eac2Output, Map<String, Object> internalData) throws ProtocolException,
	    TLVException {
	// get needed values from context
	CardVerifiableCertificate terminalCertificate;
	terminalCertificate = (CardVerifiableCertificate) internalData.get(EACConstants.IDATA_TERMINAL_CERTIFICATE);
	byte[] key = (byte[]) internalData.get(EACConstants.IDATA_PK_PCD);
	byte[] signature = (byte[]) internalData.get(EACConstants.IDATA_SIGNATURE);
	SecurityInfos securityInfos = (SecurityInfos) internalData.get(EACConstants.IDATA_SECURITY_INFOS);
	AuthenticatedAuxiliaryData aadObj;
	aadObj = (AuthenticatedAuxiliaryData) internalData.get(EACConstants.IDATA_AUTHENTICATED_AUXILIARY_DATA);


	/////////////////////////////////////////////////////////////////////
	// BEGIN TA PART
	/////////////////////////////////////////////////////////////////////
	// TA: Step 2 - MSE:SET AT
	byte[] oid = ObjectIdentifierUtils.getValue(terminalCertificate.getPublicKey().getObjectIdentifier());
	byte[] chr = terminalCertificate.getCHR().toByteArray();
	byte[] aad = aadObj.getData();

	// Calculate comp(key)
	EFCardAccess efca = new EFCardAccess(securityInfos);
	CASecurityInfos cas = efca.getCASecurityInfos();
	CADomainParameter cdp = new CADomainParameter(cas);
	CAKey caKey = new CAKey(cdp);
	caKey.decodePublicKey(key);
	byte[] compKey = caKey.getEncodedCompressedPublicKey();

	// TA: Step 4 - MSE SET AT
	ta.mseSetAT(oid, chr, compKey, aad);

	// TA: Step 4 - External Authentication
	ta.externalAuthentication(signature);
	/////////////////////////////////////////////////////////////////////
	// END TA PART
	/////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////
	// BEGIN CA PART
	/////////////////////////////////////////////////////////////////////
	// Read EF.CardSecurity
	byte[] efCardSecurity = ca.readEFCardSecurity();

	// CA: Step 1 - MSE:SET AT
	byte[] oID = ObjectIdentifierUtils.getValue(cas.getCAInfo().getProtocol());
	byte[] keyID = IntegerUtils.toByteArray(cas.getCAInfo().getKeyID());
	ca.mseSetAT(oID, keyID);

	// CA: Step 2 - General Authenticate
	byte[] responseData = ca.generalAuthenticate(key);

	TLV tlv = TLV.fromBER(responseData);
	byte[] nonce = tlv.findChildTags(0x81).get(0).getValue();
	byte[] token = tlv.findChildTags(0x82).get(0).getValue();

	// Disable Secure Messaging
	ca.destroySecureChannel();
	/////////////////////////////////////////////////////////////////////
	// END CA PART
	/////////////////////////////////////////////////////////////////////

	// Create response
	eac2Output.setEFCardSecurity(efCardSecurity);
	eac2Output.setNonce(nonce);
	eac2Output.setToken(token);

	return eac2Output;
    }

}
