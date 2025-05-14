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
 */
package org.openecard.sal.protocol.eac

import org.openecard.common.apdu.ExternalAuthentication
import org.openecard.common.apdu.GetChallenge
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.sal.protocol.exception.ProtocolException
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateChain
import org.openecard.sal.protocol.eac.apdu.MSESetATTA
import org.openecard.sal.protocol.eac.apdu.MSESetDST
import org.openecard.sal.protocol.eac.apdu.PSOVerifyCertificate

/**
 * Implements the Terminal Authentication protocol.
 * See BSI-TR-03110, version 2.10, part 2, Section B.3.4.
 * See BSI-TR-03110, version 2.10, part 3, Section B.3.
 *
 * @author Moritz Horsch
 */
class TerminalAuthentication(
	private val dispatcher: Dispatcher,
	private val slotHandle: ByteArray,
) {
	/**
	 * Verify certificates.
	 * Sends an MSE:Set DST APDU and PSO:Verify Certificate APDU per certificate. (Protocol step 1)
	 * See BSI-TR-03110, version 2.10, part 3, B.11.4.
	 * See BSI-TR-03110, version 2.10, part 3, B.11.5.
	 *
	 * @param certificateChain Certificate chain
	 * @throws ProtocolException
	 */
	@Throws(ProtocolException::class)
	fun verifyCertificates(certificateChain: CardVerifiableCertificateChain) {
		try {
			for (cvc in certificateChain.certificates) {
				// MSE:SetDST APDU
				val mseSetDST: CardCommandAPDU = MSESetDST(cvc.cAR.toByteArray())
				mseSetDST.transmit(dispatcher, slotHandle)
				// PSO:Verify Certificate  APDU
				val psovc: CardCommandAPDU = PSOVerifyCertificate(cvc.certificate.value)
				psovc.transmit(dispatcher, slotHandle)
			}
		} catch (e: APDUException) {
			throw ProtocolException(e.result)
		}
	}

	/**
	 * Initializes the Terminal Authentication protocol.
	 * Sends an MSE:Set AT APDU. (Protocol step 2)
	 * See BSI-TR-03110, version 2.10, part 3, B.11.1.
	 *
	 * @param oID Terminal Authentication object identifier
	 * @param chr Certificate Holder Reference (CHR)
	 * @param key Ephemeral public key
	 * @param aad Authenticated Auxiliary Data (AAD)
	 * @throws ProtocolException
	 */
	@Throws(ProtocolException::class)
	fun mseSetAT(
		oID: ByteArray,
		chr: ByteArray?,
		key: ByteArray?,
		aad: ByteArray?,
	) {
		try {
			val mseSetAT: CardCommandAPDU = MSESetATTA(oID, chr, key, aad)
			mseSetAT.transmit(dispatcher, slotHandle)
		} catch (e: APDUException) {
			throw ProtocolException(e.result)
		}
	}

	/**
	 * Performs an External Authentication.
	 * Sends an External Authentication APDU. (Protocol step 4)
	 * See BSI-TR-03110, version 2.10, part 3, B.11.7.
	 *
	 * @param terminalSignature Terminal signature
	 * @throws ProtocolException
	 */
	@Throws(ProtocolException::class)
	fun externalAuthentication(terminalSignature: ByteArray?) {
		try {
			val externalAuthentication: CardCommandAPDU = ExternalAuthentication(terminalSignature)
			externalAuthentication.transmit(dispatcher, slotHandle)
		} catch (e: APDUException) {
			throw ProtocolException(e.result)
		}
	}

	val challenge: ByteArray
		/**
		 * Gets a challenge from the PICC.
		 * Sends a Get Challenge APDU. (Protocol step 3)
		 * See BSI-TR-03110, version 2.10, part 3, B.11.6.
		 *
		 * @return Challenge
		 * @throws ProtocolException
		 */
		@Throws(ProtocolException::class)
		get() {
			try {
				val getChallenge: CardCommandAPDU =
					GetChallenge()
				val response =
					getChallenge.transmit(dispatcher, slotHandle)

				return response.data
			} catch (e: APDUException) {
				throw ProtocolException(e.result)
			}
		}
}
