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
 */
package org.openecard.sal.protocol.eac

import iso.std.iso_iec._24727.tech.schema.DestroyChannel
import org.openecard.common.apdu.GeneralAuthenticate
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.apdu.utils.CardUtils
import org.openecard.common.apdu.utils.FileControlParameters
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.sal.protocol.exception.ProtocolException
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.iso7816.FCP
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.ShortUtils
import org.openecard.sal.protocol.eac.apdu.MSESetATCA
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Implements the Chip Authentication protocol.
 * See BSI-TR-03110, version 2.10, part 2, Section B.3.3.
 * See BSI-TR-03110, version 2.10, part 3, Section B.2.
 *
 * @author Moritz Horsch
 */
class ChipAuthentication
/**
 * Creates a new Chip Authentication.
 *
 * @param dispatcher Dispatcher
 * @param slotHandle Slot handle
 */
(
	private val dispatcher: Dispatcher,
	private val slotHandle: ByteArray?,
) {
	/**
	 * Initializes the Chip Authentication protocol.
	 * Sends an MSE:Set AT APDU. (Protocol step 1)
	 * See BSI-TR-03110, version 2.10, part 3, B.11.1.
	 *
	 * @param oID Chip Authentication object identifier
	 * @param keyID Key identifier
	 * @throws ProtocolException
	 */
	@Throws(ProtocolException::class)
	fun mseSetAT(
		oID: ByteArray,
		keyID: ByteArray?,
	) {
		try {
			val mseSetAT: CardCommandAPDU = MSESetATCA(oID, keyID)
			mseSetAT.transmit(dispatcher, slotHandle)
		} catch (e: APDUException) {
			throw ProtocolException(e.result)
		}
	}

	/**
	 * Performs a General Authenticate.
	 * Sends an General Authenticate APDU. (Protocol step 2)
	 * See BSI-TR-03110, version 2.10, part 3, B.11.2.
	 *
	 * @param key Ephemeral Public Key
	 * @return Response APDU
	 * @throws ProtocolException
	 */
	@Throws(ProtocolException::class)
	fun generalAuthenticate(key: ByteArray): ByteArray? {
		var key = key
		try {
			if (key[0] != 0x04.toByte()) {
				key = ByteUtils.concatenate(0x04.toByte(), key)
			}
			val generalAuthenticate: CardCommandAPDU = GeneralAuthenticate(0x80.toByte(), key)
			val response = generalAuthenticate.transmit(dispatcher, slotHandle)

			return response.getData()
		} catch (e: APDUException) {
			throw ProtocolException(e.result)
		}
	}

	/**
	 * Reads the EFCardSecurity from the card.
	 *
	 * @return EFCardSecurtiy
	 * @throws ProtocolException Thrown in case there is a problem reading the file.
	 */
	@Throws(ProtocolException::class)
	fun readEFCardSecurity(): ByteArray {
		try {
			val file = ShortUtils.toByteArray(EACConstants.EF_CARDSECURITY_FID)
			val resp = CardUtils.selectFileWithOptions(dispatcher, slotHandle, file, null, FileControlParameters.FCP)
			val efCardSecurityFCP = FCP(TLV.fromBER(resp.getData()))
			val efCardSecurity = CardUtils.readFile(efCardSecurityFCP, dispatcher, slotHandle, false)
			return efCardSecurity
		} catch (ex: APDUException) {
			throw ProtocolException(ex.result)
		} catch (ex: TLVException) {
			throw ProtocolException("Failed to parse FCP.", ex)
		}
	}

	/**
	 * Destroys a previously established PACE channel.
	 */
	fun destroySecureChannel() {
		val destroyChannel = DestroyChannel()
		destroyChannel.setSlotHandle(slotHandle)
		dispatcher.safeDeliver(destroyChannel)
	}

	companion object {
		private val LOG: Logger? = LoggerFactory.getLogger(ChipAuthentication::class.java)
	}
}
