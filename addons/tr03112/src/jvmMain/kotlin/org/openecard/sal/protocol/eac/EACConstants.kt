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
package org.openecard.sal.protocol.eac

/**
 * Defines constants for the EAC protocol.
 *
 * @author Moritz Horsch
 */
object EACConstants {
	// EF.CardSecurity file identifier
	val EF_CARDSECURITY_FID: Short = 0x011D.toShort()

	// Internal data
	const val IDATA_CERTIFICATES: String = "Certificates"
	const val IDATA_AUTHENTICATED_AUXILIARY_DATA: String = "AuthenticatedAuxiliaryData"
	const val IDATA_PK_PCD: String = "PKPCD"
	const val IDATA_SECURITY_INFOS: String = "SecurityInfos"
	const val IDATA_CURRENT_CAR: String = "CurrentCAR"
	const val IDATA_PREVIOUS_CAR: String = "PreviousCAR"
	const val IDATA_CHALLENGE: String = "Challenge"
	const val IDATA_SIGNATURE: String = "Signature"
	const val IDATA_TERMINAL_CERTIFICATE: String = "TerminalCertificate"
}
