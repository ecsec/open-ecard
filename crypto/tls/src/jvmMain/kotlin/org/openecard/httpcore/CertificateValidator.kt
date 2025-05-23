/****************************************************************************
 * Copyright (C) 2013-2019 ecsec GmbH.
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
package org.openecard.httpcore

import org.openecard.bouncycastle.tls.TlsServerCertificate
import java.net.URL

/**
 * Validation interface for TLS certificate validation.
 * The interface can be used to add security checks like e.g. the ones defined in TR-03112.
 *
 * @author Tobias Wich
 */
interface CertificateValidator {
	/**
	 * Result indicating whether to proceed or stop execution.
	 * Errors in the validation are signaled with exceptions in the `validate` function itself.
	 */
	enum class VerifierResult {
		CONTINUE,
		DONTCARE,
		FINISH,
	}

	/**
	 * Validate the given tuple.
	 *
	 * @param url Url of the last connection.
	 * @param cert Certificate chain of the last connection.
	 * @return Status indicating how to proceed.
	 * @throws ValidationError Thrown in case the validation failed.
	 */
	@Throws(ValidationError::class)
	fun validate(
		url: URL,
		cert: TlsServerCertificate,
	): VerifierResult
}
