/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
package org.openecard.common.interfaces

import iso.std.iso_iec._24727.tech.schema.CardInfoType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import java.io.InputStream

/**
 * Interface providing a method to get a CardInfo file for a specific card.
 * This is usually implemented by the SAL managing the card, so it is able to add specific information such as
 * certificates.
 *
 * @author Tobias Wich
 */
interface CIFProvider {
	/**
	 * Gets the CardInfo file for a given cardType enriched with information of the card the handle points to.
	 *
	 * @param handle Handle referencing a card. May be `null` in case only the static CIF will be returned.
	 * @param cardType Type of the card to look up. Without this identifier, the static CIF template can not be found.
	 * @return The CIF or `null` in case there is no CIF or the CIF could not be created correctly.
	 * @throws RuntimeException Thrown in case there is an error due to invalid code or invalid hardware.
	 */
	@Throws(RuntimeException::class)
	fun getCardInfo(
		handle: ConnectionHandleType?,
		cardType: String,
	): CardInfoType?

	/**
	 * Gets the CardInfo file for a given cardType enriched with information of the card.
	 *
	 * @param cardType Type of the card.
	 * @return The CIF or `null` in case there is no CIF or the CIF could not be created correctly.
	 */
	@Throws(RuntimeException::class)
	fun getCardInfo(cardType: String): CardInfoType?

	/**
	 * Gets the CardImage for a given cardType. Returns null if the card type is not known to the CIF-Provider.
	 *
	 * @param cardType Type of the card.
	 * @return The Card Image as InputStream or `null` in case there is no Card Image.
	 */
	fun getCardImage(cardType: String): InputStream?

	/**
	 * Evaluates whether recognition is necessary for cards with the given ATR.
	 * If there are conflicting views between the specialized SALs, then a result of `false` wins, as it means one
	 * particular SAL knows that this ATR is handled by it, so no recognition is needed.
	 *
	 * @param atr ATR of the card in question.
	 * @return `true` if recognition must be performed, `false` otherwise.
	 */
	fun needsRecognition(atr: ByteArray): Boolean
}
