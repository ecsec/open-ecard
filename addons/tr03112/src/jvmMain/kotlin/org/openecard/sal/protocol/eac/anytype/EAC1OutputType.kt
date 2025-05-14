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
import iso.std.iso_iec._24727.tech.schema.EAC1OutputType
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.util.ByteUtils

/**
 * Implements the EAC1OutputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.5.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class EAC1OutputType(
	private val authMap: AuthDataMap,
) {
	private var retryCounter: Int? = null
	private var chat: ByteArray? = null
	private var currentCar: ByteArray? = null
	private var previousCar: ByteArray? = null
	private var efCardAccess: ByteArray? = null
	private var idpicc: ByteArray? = null
	private var challenge: ByteArray? = null

	/**
	 * Sets the retry counter.
	 *
	 * @param retryCounter Retry counter.
	 */
	fun setRetryCounter(retryCounter: Int?) {
		this.retryCounter = retryCounter
	}

	/**
	 * Sets the Certificate Holder Authorization Template (CHAT).
	 *
	 * @param chat Certificate Holder Authorization Template (CHAT).
	 */
	fun setCHAT(chat: ByteArray?) {
		this.chat = chat
	}

	/**
	 * Sets the most recent Certification Authority Reference (CAR).
	 *
	 * @param car Certification Authority Reference (CAR).
	 */
	fun setCurrentCAR(car: ByteArray) {
		this.currentCar = car
	}

	/**
	 * Sets the previous Certification Authority Reference (CAR).
	 *
	 * @param car Certification Authority Reference (CAR).
	 */
	fun setPreviousCAR(car: ByteArray?) {
		this.previousCar = car
	}

	/**
	 * Sets the file content of the EF.CardAccess.
	 *
	 * @param efCardAccess EF.CardAccess
	 */
	fun setEFCardAccess(efCardAccess: ByteArray?) {
		this.efCardAccess = efCardAccess
	}

	/**
	 * Sets the card identifier ID_PICC.
	 *
	 * @param idpicc Card identifier ID_PICC.
	 */
	fun setIDPICC(idpicc: ByteArray?) {
		this.idpicc = idpicc
	}

	/**
	 * Sets the challenge.
	 *
	 * @param challenge Challenge.
	 */
	fun setChallenge(challenge: ByteArray?) {
		this.challenge = challenge
	}

	val authDataType: DIDAuthenticationDataType?
		/**
		 * Returns the DIDAuthenticationDataType.
		 *
		 * @return DIDAuthenticationDataType
		 */
		get() {
			val authResponse: AuthDataResponse<EAC1OutputType> =
				authMap.createResponse(EAC1OutputType())
			if (retryCounter != null) {
				authResponse.addElement(
					RETRY_COUNTER,
					retryCounter.toString(),
				)
			}
			authResponse.addElement(
				CHAT,
				ByteUtils.toHexString(chat),
			)
			authResponse.addElement(
				CAR,
				String(currentCar!!),
			)
			if (previousCar != null) {
				authResponse.addElement(
					CAR,
					String(previousCar!!),
				)
			}
			authResponse.addElement(
				EF_CARDACCESS,
				ByteUtils.toHexString(efCardAccess),
			)
			authResponse.addElement(
				ID_PICC,
				ByteUtils.toHexString(idpicc),
			)
			authResponse.addElement(
				CHALLENGE,
				ByteUtils.toHexString(challenge),
			)

			return authResponse.response
		}

	companion object {
		const val RETRY_COUNTER: String = "RetryCounter"
		const val CHAT: String = "CertificateHolderAuthorizationTemplate"
		const val CAR: String = "CertificationAuthorityReference"
		const val EF_CARDACCESS: String = "EFCardAccess"
		const val ID_PICC: String = "IDPICC"
		const val CHALLENGE: String = "Challenge"
	}
}
