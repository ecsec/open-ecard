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
 */
package org.openecard.binding.tctoken

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.common.DynamicContext
import org.openecard.common.util.Promise
import org.openecard.common.util.TR03112Utils
import org.openecard.crypto.common.asn1.cvc.CertificateDescription
import org.openecard.httpcore.CertificateValidator
import org.openecard.httpcore.ValidationError
import org.openecard.i18n.I18N
import java.net.MalformedURLException
import java.net.URL

/**
 * Implementation performing the redirect checks according to TR-03112.
 * The checks are described in BSI TR-03112 sec. 3.4.5.
 *
 * @author Tobias Wich
 */

private val LOG = KotlinLogging.logger { }

class RedirectCertificateValidator(
	redirectChecks: Boolean,
) : CertificateValidator {
	private val descPromise: Promise<Any>
	private val redirectChecks: Boolean

	private var certDescExists = false
	private var lastURL: URL? = null

	/**
	 * Creates an object of this class bound to the values in the current dynamic context.
	 *
	 * @param redirectChecks True if the TR-03112 checks must be performed.
	 */
	init {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
		descPromise = dynCtx.getPromise(TR03112Keys.ESERVICE_CERTIFICATE_DESC)
		this.redirectChecks = redirectChecks
	}

	override fun validate(
		url: URL,
		cert: TlsServerCertificate,
	): CertificateValidator.VerifierResult {
		try {
			// disable certificate checks according to BSI TR03112-7 in some situations
			if (redirectChecks) {
				val desc = descPromise.derefNonblocking() as CertificateDescription?
				certDescExists = desc != null

				val host =
					url.protocol + "://" + url.host + (if (url.port == -1) "" else (":" + url.port))
				// check points certificate (but just in case we have a certificate description)
				if (certDescExists && !TR03112Utils.isInCommCertificates(cert, desc!!.getCommCertificates(), host)) {
					LOG.error {
						"The retrieved server certificate is NOT contained in the CommCertificates of " +
							"the CertificateDescription extension of the eService certificate."
					}
					throw ValidationError(
						I18N.strings.tr03112_redirect_cert_validator_invalid_redirect.localized(),
					)
				}

				// check if we match the SOP
				val sopUrl: URL
				if (certDescExists && desc!!.subjectURL != null && !desc.subjectURL!!.isEmpty()) {
					sopUrl = URL(desc.subjectURL)
				} else {
					val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
					sopUrl = dynCtx.get(TR03112Keys.TCTOKEN_URL) as URL
				}
				// determine the URL that has to be SOP checked (TR-03124 Determine refreshURL)
				// on th efirst invocation this is the current URL, on the following invocations this is the last used
				if (lastURL == null) {
					lastURL = url
				}

				// check SOP for last URL and update the URL
				val sop = TR03112Utils.checkSameOriginPolicy(lastURL!!, sopUrl)
				lastURL = url
				return if (!sop) {
					// there is more to come
					CertificateValidator.VerifierResult.CONTINUE
				} else {
					// SOP fulfilled
					CertificateValidator.VerifierResult.FINISH
				}
			} else {
				// without the nPA there is no sensible exit point and as a result the last call is executed twice
				// in that case its equally valid to let the browser do the redirects
				return CertificateValidator.VerifierResult.FINISH
			}
		} catch (ex: MalformedURLException) {
			throw ValidationError(
				I18N.strings.tr03112_redirect_cert_validator_malformed_subject_url.localized(),
				ex,
			)
		}
	}
}
