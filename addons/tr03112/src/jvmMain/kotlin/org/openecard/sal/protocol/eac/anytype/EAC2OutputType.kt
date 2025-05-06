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
 */
package org.openecard.sal.protocol.eac.anytype

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.EAC2OutputType
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.util.ByteUtils

/**
 * Implements the EAC2OutputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.6.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class EAC2OutputType(
	private val authMap: AuthDataMap,
) {
	private var challenge: ByteArray? = null
	private var efCardSecurity: ByteArray? = null
	private var token: ByteArray? = null
	private var nonce: ByteArray? = null

	/**
	 * Sets the challenge.
	 *
	 * @param challenge Challenge value.
	 */
	fun setChallenge(challenge: ByteArray?) {
		this.challenge = challenge
	}

	/**
	 * Sets the file content of the EF.CardSecurity.
	 *
	 * @param efCardSecurity EF.CardSecurity
	 */
	fun setEFCardSecurity(efCardSecurity: ByteArray?) {
		this.efCardSecurity = efCardSecurity
	}

	/**
	 * Sets the nonce r_PICC,CA.
	 *
	 * @param nonce Nonce r_PICC,CA
	 */
	fun setNonce(nonce: ByteArray?) {
		this.nonce = nonce
	}

	/**
	 * Sets the AuthenticationToken T_PICC.
	 *
	 * @param token AuthenticationToken T_PICC
	 */
	fun setToken(token: ByteArray?) {
		this.token = token
	}

	val authDataType: DIDAuthenticationDataType?
		/**
		 * Returns the DIDAuthenticationDataType.
		 *
		 * @return DIDAuthenticationDataType
		 */
		get() {
			val authResponse: AuthDataResponse<*> =
				authMap.createResponse<EAC2OutputType?>(EAC2OutputType())
			if (challenge != null) {
				authResponse.addElement(
					CHALLENGE,
					ByteUtils.toHexString(challenge),
				)
			} else {
				authResponse.addElement(
					EF_CARDSECURITY,
					ByteUtils.toHexString(efCardSecurity),
				)
				authResponse.addElement(
					TOKEN,
					ByteUtils.toHexString(token),
				)
				authResponse.addElement(
					NONCE,
					ByteUtils.toHexString(nonce),
				)
			}
			return authResponse.getResponse()
		}

	companion object {
		const val CHALLENGE: String = "Challenge"
		const val EF_CARDSECURITY: String = "EFCardSecurity"
		const val TOKEN: String = "AuthenticationToken"
		const val NONCE: String = "Nonce"
	}
}
