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
 * PERFORM SECURITY OPERATION Command
 * See ISO/IEC 7816-8 Section 11
 *
 * @author Moritz Horsch
 */
open class PerformSecurityOperation : CardCommandAPDU {
	/**
	 * Creates a new PERFORM SECURITY OPERATION Command.
	 */
	constructor() : super(
		x00,
		PERFORM_SECURITY_OPERATION_INS,
		x00,
		x00,
	)

	/**
	 * Creates a new PERFORM SECURITY OPERATION APDU.
	 *
	 * @param p1 Parameter byte P1
	 * @param p2 Parameter byte P2
	 */
	constructor(p1: Byte, p2: Byte) : super(x00, PERFORM_SECURITY_OPERATION_INS, p1, p2)

	companion object {
		/**
		 * PERFORM SECURITY OPERATION command instruction byte
		 */
		private const val PERFORM_SECURITY_OPERATION_INS = 0x2A.toByte()
	}
}
