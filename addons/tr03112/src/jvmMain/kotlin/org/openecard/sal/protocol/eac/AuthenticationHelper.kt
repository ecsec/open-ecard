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
 */
package org.openecard.sal.protocol.eac

import org.openecard.common.sal.protocol.exception.ProtocolException
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.util.IntegerUtils
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate
import org.openecard.crypto.common.asn1.eac.AuthenticatedAuxiliaryData
import org.openecard.crypto.common.asn1.eac.CADomainParameter
import org.openecard.crypto.common.asn1.eac.SecurityInfos
import org.openecard.crypto.common.asn1.eac.ef.EFCardAccess
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils.getValue
import org.openecard.sal.protocol.eac.anytype.EAC2OutputType
import org.openecard.sal.protocol.eac.crypto.CAKey

/**
 * Helper class combining TA and CA
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class AuthenticationHelper(private val ta: TerminalAuthentication, private val ca: ChipAuthentication) {
    @Throws(ProtocolException::class, TLVException::class)
    fun performAuth(eac2Output: EAC2OutputType, internalData: MutableMap<String?, Any?>): EAC2OutputType {
        // get needed values from context
        val terminalCertificate: CardVerifiableCertificate
        terminalCertificate = internalData.get(EACConstants.IDATA_TERMINAL_CERTIFICATE) as CardVerifiableCertificate
        val key = internalData.get(EACConstants.IDATA_PK_PCD) as ByteArray
        val signature = internalData.get(EACConstants.IDATA_SIGNATURE) as ByteArray?
        val securityInfos = internalData.get(EACConstants.IDATA_SECURITY_INFOS) as SecurityInfos
        val aadObj: AuthenticatedAuxiliaryData
        aadObj = internalData.get(EACConstants.IDATA_AUTHENTICATED_AUXILIARY_DATA) as AuthenticatedAuxiliaryData


        /**////////////////////////////////////////////////////////////////// */
        // BEGIN TA PART
        /**////////////////////////////////////////////////////////////////// */
        // TA: Step 2 - MSE:SET AT
        val oid = getValue(terminalCertificate.getPublicKey().objectIdentifier)
        val chr = terminalCertificate.cHR.toByteArray()
        val aad = aadObj.data

        // Calculate comp(key)
        val efca = EFCardAccess.getInstance(securityInfos)
        val cas = efca.cASecurityInfos
        val cdp = CADomainParameter(cas)
        val caKey = CAKey(cdp)
        caKey.decodePublicKey(key)
        val compKey = caKey.getEncodedCompressedPublicKey()

        // TA: Step 4 - MSE SET AT
        ta.mseSetAT(oid, chr, compKey, aad)

        // TA: Step 4 - External Authentication
        ta.externalAuthentication(signature)

        /**////////////////////////////////////////////////////////////////// */
        // END TA PART
        /**////////////////////////////////////////////////////////////////// */ /**////////////////////////////////////////////////////////////////// */
        // BEGIN CA PART
        /**////////////////////////////////////////////////////////////////// */
        // Read EF.CardSecurity
        val efCardSecurity = ca.readEFCardSecurity()

        // CA: Step 1 - MSE:SET AT
        val oID = getValue(cas.cAInfo!!.protocol)
        val keyID = IntegerUtils.toByteArray(cas.cAInfo!!.keyID)
        ca.mseSetAT(oID, keyID)

        // CA: Step 2 - General Authenticate
        val responseData = ca.generalAuthenticate(key)

        val tlv = TLV.fromBER(responseData)
        val nonce = tlv.findChildTags(0x81).get(0).getValue()
        val token = tlv.findChildTags(0x82).get(0).getValue()

        // Disable Secure Messaging
        ca.destroySecureChannel()

        /**////////////////////////////////////////////////////////////////// */
        // END CA PART
        /**////////////////////////////////////////////////////////////////// */

        // Create response
        eac2Output.setEFCardSecurity(efCardSecurity)
        eac2Output.setNonce(nonce)
        eac2Output.setToken(token)

        return eac2Output
    }
}
