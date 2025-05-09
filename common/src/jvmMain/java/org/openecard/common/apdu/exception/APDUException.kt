/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.common.apdu.exception

import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.common.ECardException
import org.openecard.common.WSHelper.WSException
import org.openecard.common.apdu.common.CardResponseAPDU

/**
 * @author Moritz Horsch
 */
class APDUException : ECardException {
	/**
	 * Returns the TransmitResponse.
	 *
	 * @return TransmitResponseTransmitResponse
	 */
	var transmitResponse: TransmitResponse? = null
		private set

	/**
	 * Returns the ResponseAPDU.
	 *
	 * @return ResponseAPDU
	 */
	var responseAPDU: CardResponseAPDU? = null
		private set

	/**
	 * Creates a new APDUException.
	 *
	 * @param msg Message
	 */
	constructor(msg: String) : super(makeOasisResultTraitImpl(msg), null)

	/**
	 * Creates a new APDUException.
	 *
	 * @param minor Minor message
	 * @param msg Message
	 */
	constructor(minor: String, msg: String?) : super(makeOasisResultTraitImpl(minor, msg), null)

	/**
	 * Creates a new APDUException.
	 *
	 * @param r Result
	 */
	constructor(r: Result) : super(makeOasisResultTraitImpl(r), null)

	/**
	 * Creates a new APDUException.
	 *
	 * @param cause Cause
	 */
	constructor(cause: Throwable?) : super(makeOasisResultTraitImpl(), cause)

	/**
	 * Creates a new APDUException.
	 *
	 * @param ex WSException
	 */

	constructor(cause: Throwable?, r: Result) : super(makeOasisResultTraitImpl(r), cause)

	/**
	 * Creates a new APDUException.
	 *
	 * @param cause Cause
	 * @param tr TransmitResponse
	 */
	constructor(cause: Throwable?, tr: TransmitResponse) : this(cause) {
		transmitResponse = tr
		if (tr.outputAPDU.isNotEmpty()) {
			responseAPDU = CardResponseAPDU(tr)
		}
	}

	/**
	 * Creates a new APDUException.
	 *
	 * @param ex WSException
	 * @param tr TransmitResponse
	 */
	constructor(ex: WSException?, tr: TransmitResponse) : this(ex) {
		transmitResponse = tr
		if (!tr.outputAPDU.isEmpty()) {
			responseAPDU = CardResponseAPDU(tr)
		}
	}

	companion object {
		private const val serialVersionUID = 1L
	}
}
