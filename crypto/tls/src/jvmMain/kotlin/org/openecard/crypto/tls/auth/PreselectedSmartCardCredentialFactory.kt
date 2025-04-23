/****************************************************************************
 * Copyright (C) 2013-2023 ecsec GmbH.
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
package org.openecard.crypto.tls.auth

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.bouncycastle.tls.CertificateRequest
import org.openecard.bouncycastle.tls.TlsCredentialedSigner
import org.openecard.common.interfaces.Dispatcher

/**
 * Implementation of CredentialFactory operating on generic crypto SAL DIDs.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
class PreselectedSmartCardCredentialFactory(
	dispatcher: Dispatcher,
	private val inputHandle: ConnectionHandleType,
	filterAlwaysReadable: Boolean,
) : BaseSmartCardCredentialFactory(dispatcher, filterAlwaysReadable) {
	override val usedHandle: ConnectionHandleType?
		get() {
			return inputHandle
		}

	override fun getClientCredentials(cr: CertificateRequest): List<TlsCredentialedSigner> {
		// use the one prepared handle
		return getClientCredentialsForCard(cr, inputHandle)
	}
}
