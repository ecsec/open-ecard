/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
package org.openecard.common.ifd.anytype

import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.util.ByteUtils

/**
 * Implements the PACEOutputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.3.5.
 *
 * @author Tobias Wich
 */
class PACEOutputType
/**
 * Creates a new PACEOutputType.
 *
 * @param authMap AuthDataMap
 */
(
	private val authMap: AuthDataMap
) {
	private var efCardAccess: ByteArray? = null
	private var currentCAR: ByteArray? = null
	private var previousCAR: ByteArray? = null
	private var idpicc: ByteArray? = null
	private var retryCounter: Byte = 0

	/**
	 * Sets the content of the file EF.CardAccess.
	 *
	 * @param efCardAccess Content of the file EF.CardAccess
	 */
	fun setEFCardAccess(efCardAccess: ByteArray?) {
		this.efCardAccess = efCardAccess
	}

	/**
	 * Sets the current CAR.
	 *
	 * @param car current CAR
	 */
	fun setCurrentCAR(car: ByteArray?) {
		this.currentCAR = car
	}

	/**
	 * Sets the previous CAR.
	 *
	 * @param car Previous CAR
	 */
	fun setPreviousCAR(car: ByteArray?) {
		this.previousCAR = car
	}

	/**
	 * Sets the IDPICC.
	 *
	 * @param idpicc IDPICC
	 */
	fun setIDPICC(idpicc: ByteArray?) {
		this.idpicc = idpicc
	}

	/**
	 * Sets the retry counter.
	 *
	 * @param counter Retry counter
	 */
	fun setRetryCounter(counter: Byte) {
		this.retryCounter = counter
	}

	val authDataType: iso.std.iso_iec._24727.tech.schema.PACEOutputType
		/**
		 * Returns the DIDAuthenticationDataType.
		 *
		 * @return DIDAuthenticationDataType
		 */
		get() {
			val authResponse = authMap.createResponse(iso.std.iso_iec._24727.tech.schema.PACEOutputType())
			authResponse.addElement(
				RETRY_COUNTER,
				retryCounter.toInt().toString()
			)
			authResponse.addElement(
				EF_CARD_ACCESS,
				ByteUtils.toHexString(efCardAccess)
			)
			if (currentCAR != null) {
				authResponse.addElement(
					CURRENT_CAR,
					ByteUtils.toHexString(currentCAR)
				)
			}
			if (previousCAR != null) {
				authResponse.addElement(
					PREVIOUS_CAR,
					ByteUtils.toHexString(previousCAR)
				)
			}
			if (idpicc != null) {
				authResponse.addElement(
					ID_PICC,
					ByteUtils.toHexString(idpicc)
				)
			}
			return authResponse.getResponse()
		}

	companion object {
		const val RETRY_COUNTER: String = "RetryCounter"
		const val EF_CARD_ACCESS: String = "EFCardAccess"
		const val CURRENT_CAR: String = "CARcurr"
		const val PREVIOUS_CAR: String = "CARprev"
		const val ID_PICC: String = "IDPICC"
	}
}
