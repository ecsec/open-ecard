/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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
package org.openecard.transport.httpcore

import org.openecard.bouncycastle.tls.*
import org.openecard.bouncycastle.tls.crypto.TlsCrypto
import java.io.IOException

/**
 * Implementation of BouncyCastle's abstract DefaultTlsClient.
 *
 * @author Tobias Wich
 */
open class DefaultTlsClientImpl(
	crypto: TlsCrypto,
) : DefaultTlsClient(crypto) {
	@Throws(IOException::class)
	override fun getAuthentication(): TlsAuthentication =
		object : TlsAuthentication {
			@Throws(IOException::class)
			override fun notifyServerCertificate(serverCertificate: TlsServerCertificate) {
				// ignore
			}

			@Throws(IOException::class)
			override fun getClientCredentials(cr: CertificateRequest): TlsCredentials =
				throw UnsupportedOperationException("Not supported yet.")
		}
}
