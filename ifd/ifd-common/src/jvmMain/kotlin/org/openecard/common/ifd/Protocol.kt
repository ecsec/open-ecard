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
 ***************************************************************************/

package org.openecard.common.ifd

import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import org.openecard.common.interfaces.Dispatcher
import org.openecard.gui.UserConsent

/**
 *
 * @author Tobias Wich
 */
// TODO: replace with addon IFD protocols
interface Protocol {
	/**
	 * Perform protocol and thereby set up a secure messaging channel.
	 *
	 * @param req Request data needed for the protocol
	 * @param dispatcher Dispatcher containing IFD instance to perform commands on the terminal
	 * @param gui UserConsent GUI which can be used to get secrets (e.g. PIN) from the user
	 * @return Protocol response data
	 */
	fun establish(
		req: EstablishChannel,
		dispatcher: Dispatcher,
		gui: UserConsent,
	): EstablishChannelResponse

	/**
	 * Filter function to perform secure messaging after the protocol has been established.<br></br>
	 * Apply secure messaging encryption to APDU.
	 *
	 * @param commandAPDU Command APDU which should be encrypted
	 * @return Command APDU which is encrypted
	 */
	@Throws(
		InvalidInputApduInSecureMessaging::class,
		SecureMessagingCryptoException::class,
		MissingSecureMessagingChannel::class,
	)
	fun applySM(commandAPDU: ByteArray): ByteArray

	/**
	 * Filter function to perform secure messaging after the protocol has been established.<br></br>
	 * Remove secure messaging encryption from APDU.
	 *
	 * @param responseAPDU Response APDU which should be decrypted
	 * @return Response APDU which is encrypted
	 */
	@Throws(
		SecureMessagingParseException::class,
		SecureMessagingCryptoException::class,
		SecureMessagingRejectedByIcc::class,
		UnsupportedSecureMessagingFeature::class,
		MissingSecureMessagingChannel::class,
	)
	fun removeSM(responseAPDU: ByteArray): ByteArray
}
