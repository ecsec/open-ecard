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
package org.openecard.sal.protocol.eac.apdu

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.apdu.ManageSecurityEnvironment
import org.openecard.common.apdu.common.CardAPDUOutputStream
import java.io.IOException

private val logger = KotlinLogging.logger { }

/**
 * Implements a new MSE:Set AT APDU for Terminal Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.1.
 * See ISO/IEC 7816-4, Section 7.5.11.
 *
 * @author Moritz Horsch
 */
class MSESetATTA : ManageSecurityEnvironment {
	/**
	 * Creates a new MSE:Set AT for Terminal Authentication.
	 */
	constructor() : super(0x81.toByte(), AT)

	/**
	 * Creates a new MSE:Set AT for Terminal Authentication.
	 *
	 * @param oID Terminal Authentication object identifier
	 * @param chr Certificate Holder Reference
	 * @param pkPCD Ephemeral Public Key
	 * @param aad Auxiliary Data Verification
	 */
	constructor(oID: ByteArray, chr: ByteArray?, pkPCD: ByteArray?, aad: ByteArray?) : super(0x81.toByte(), AT) {
		val caos = CardAPDUOutputStream()
		try {
			caos.writeTLV(0x80.toByte(), oID)

			if (chr != null) {
				caos.writeTLV(0x83.toByte(), chr)
			}
			if (pkPCD != null) {
				caos.writeTLV(0x91.toByte(), pkPCD)
			}
			if (aad != null) {
				caos.write(aad)
			}

			caos.flush()
		} catch (e: IOException) {
			logger.error(e) { "${e.message}" }
		} finally {
			try {
				caos.close()
			} catch (ignore: IOException) {
			}
		}

		data = caos.toByteArray()
	}
}
