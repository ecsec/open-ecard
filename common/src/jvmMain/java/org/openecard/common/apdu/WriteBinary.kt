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
package org.openecard.common.apdu

import org.openecard.common.apdu.common.CardCommandAPDU

/**
 * WRITE BINARY Command.
 * See ISO/IEC 7816-4 Section 7.2.4
 *
 * APDU: 0x00 0xD0|0xD1 P1 P2 Lc Data.
 *
 * @author Hans-Martin Haase
 */
class WriteBinary
/**
	 * Creates a new WRITE BINARY APDU.
	 * APDU: 0x00 0xD0|0xD1 P1 P2 Lc Data.
	 *
	 * @param ins The instruction byte for the Write Binary command. There are two public variables in this class for
	 * defining the instruction byte.
	 * @param p1 P1 according to ISO 7816 part 4 section 7.2.2.
	 * @param p2 P2 according to ISO 7816 part 4 section 7.2.2.
	 * @param dataToWrite The data which shall be written to the file.
	 */
	constructor(
		ins: Byte,
		p1: Byte,
		p2: Byte,
		dataToWrite: ByteArray,
	) : CardCommandAPDU(x00, ins, p1, p2, dataToWrite) {
		companion object {
			const val INS_WRITE_BINARY_DATA: Byte = 0xD0.toByte()

			const val INS_WRITE_BINARY_OFFSET_DISCRETIONARY_DATA: Byte = 0xD1.toByte()
		}
	}
