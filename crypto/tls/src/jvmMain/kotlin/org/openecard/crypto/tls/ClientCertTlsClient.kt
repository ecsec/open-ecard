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
package org.openecard.crypto.tls

import org.openecard.bouncycastle.tls.ProtocolVersion
import org.openecard.bouncycastle.tls.TlsAuthentication
import org.openecard.bouncycastle.tls.TlsClient
import org.openecard.common.AppVersion.version

/**
 * Interface extending the BouncyCastle TlsClient interface with externally settable TlsAuthentication implementations.
 *
 * @author Tobias Wich
 */
interface ClientCertTlsClient : TlsClient {
	/**
	 * Sets the TlsAuthentication implementation that should be used when the server requests authentication.
	 *
	 * @param tlsAuth TlsAuthentication implementation.
	 */
	fun setAuthentication(tlsAuth: TlsAuthentication?)

	/**
	 * Get the desired TLS protocol version.
	 *
	 * @return Returns the TLS protocol version.
	 */

	/**
	 * Sets the desired TLS protocol version.
	 *
	 * @param version TLS protocol version.
	 */
	var clientVersion: ProtocolVersion

	/**
	 * Sets the minimum accepted TLS protocol version.
	 * In case the server tries to use a lower version, an error will occur in the TLS stack.
	 *
	 * @param minClientVersion Minimum accepted TLS protocol version.
	 */
	fun setMinimumVersion(minClientVersion: ProtocolVersion)
}
