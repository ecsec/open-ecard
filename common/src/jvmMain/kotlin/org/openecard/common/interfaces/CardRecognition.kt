/****************************************************************************
 * Copyright (C) 2016-2017 ecsec GmbH.
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
import java.math.BigInteger

/**
 *
 * @author Tobias Wich
 */
interface CardRecognition {
	val cardInfos: List<CardInfoType>

	// TODO: get rid of these functions, they should be in the SAL propably
	fun getCardInfo(type: String): CardInfoType?

	fun getCardInfoFromRepo(type: String): CardInfoType?

	fun getTranslatedCardName(cardType: String): String

	fun getCardImage(objectid: String): InputStream?

	val unknownCardImage: InputStream

	val noCardImage: InputStream

	val noTerminalImage: InputStream

	@Throws(RecognitionException::class)
	fun recognizeCard(
		ctx: ByteArray,
		ifdName: String,
		slot: BigInteger,
	): ConnectionHandleType.RecognitionInfo?
}
