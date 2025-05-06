/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import org.openecard.crypto.common.asn1.cvc.CHAT
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate
import org.openecard.crypto.common.asn1.cvc.CertificateDescription
import org.openecard.crypto.common.asn1.eac.AuthenticatedAuxiliaryData

/**
 * Data structure carrying all information from DID request and the user interaction.
 *
 * @author Tobias Wich
 */
class EACData {
	/**
	 * CVC Step Data
	 */
	lateinit var certificate: CardVerifiableCertificate
	lateinit var certificateDescription: CertificateDescription
	var rawCertificateDescription: ByteArray? = null

	@JvmField
	var transactionInfo: String? = null

	/**
	 * CHAT Step Data
	 */
	lateinit var requiredCHAT: CHAT
	lateinit var optionalCHAT: CHAT
	lateinit var selectedCHAT: CHAT
	lateinit var aad: AuthenticatedAuxiliaryData

	/**
	 * PIN Step Data
	 */
	@JvmField
	var pinID: Byte = 0
	var pin: String? = null

	/**
	 * DID Data needed for execution
	 */
	lateinit var didRequest: DIDAuthenticate
	var paceResponse: EstablishChannelResponse? = null
}
