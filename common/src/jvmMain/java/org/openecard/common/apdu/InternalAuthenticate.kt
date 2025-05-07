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
package org.openecard.common.apdu

import org.openecard.common.apdu.common.CardCommandAPDU

/**
 * INTERNAL AUTHENTICATE command.
 * See ISO/IEC 7816-4 Section 7.5.2
 *
 * @author Moritz Horsch
 */
class InternalAuthenticate : CardCommandAPDU {
	/**
	 * Creates a new INTERNAL AUTHENTICATE command.
	 * APDU: 0x00 0x88 0x00 0x00
	 */
	constructor() : super(
		x00,
		INTERNAL_AUTHENTICATE_INS,
		x00,
		x00,
	)

	/**
	 * Creates a new INTERNAL AUTHENTICATE command.
	 * APDU: 0x00 0x88 0x00 0x00 LC DATA
	 *
	 * @param data Data
	 */
	constructor(data: ByteArray?) : super(
		x00,
		INTERNAL_AUTHENTICATE_INS,
		x00,
		x00,
	) {
		this.data = data!!
	}

	/**
	 * Creates a new INTERNAL AUTHENTICATE command.
	 * APDU: 0x00 0x88 0x00 0x00 LC DATA
	 *
	 * @param data Data
	 * @param le expected length of the response
	 */
	constructor(data: ByteArray?, le: Byte) : super(
		x00,
		INTERNAL_AUTHENTICATE_INS,
		x00,
		x00,
	) {
		this.data = data!!
		setLE(le)
	}

	companion object {
		/**
		 * INTERNAL AUTHENTICATION command instruction byte
		 */
		private const val INTERNAL_AUTHENTICATE_INS = 0x88.toByte()
	}
}
