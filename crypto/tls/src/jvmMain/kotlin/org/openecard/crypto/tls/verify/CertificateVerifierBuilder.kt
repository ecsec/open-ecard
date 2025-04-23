/****************************************************************************
 * Copyright (C) 2014-2017 ecsec GmbH.
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
package org.openecard.crypto.tls.verify

import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.crypto.tls.CertificateVerificationException
import org.openecard.crypto.tls.CertificateVerifier
import java.util.*

/**
 * Builder class for CertificateVerifier instances.
 * This class helps to combine different verifiers with AND and OR operators.
 *
 * The builder class is mutable, however the built CertificateVerifier instances are not.
 *
 * @author Tobias Wich
 */
class CertificateVerifierBuilder private constructor(
	private val parent: CertificateVerifierBuilder?,
) {
	private val orChilds = mutableListOf<CertificateVerifierBuilder>()
	private val andList = mutableListOf<CertificateVerifier>()

	/**
	 * Creates an empty builder instance.
	 * The empty builder node can be either an AND or an OR node, depending how it is initialized.
	 */
	constructor() : this(null)

	/**
	 * Adds all verifiers which are checked together with all other verifiers in a boolean AND expression.
	 * This function may only be called when no OR function has been called on this node before.
	 *
	 * @param verifier The list of verifiers to add.
	 * @return This instance of the builder to enable command chaining.
	 * @throws IllegalArgumentException Thrown in case this builder node is already an OR node.
	 */
	fun and(vararg verifier: CertificateVerifier): CertificateVerifierBuilder {
		if (orChilds.isEmpty()) {
			return and(listOf<CertificateVerifier>(*verifier))
		} else {
			throw IllegalStateException("The CertificateVerifierBuilder already contains OR elements.")
		}
	}

	/**
	 * Adds all verifiers which are checked together with all other verifiers in a boolean AND expression.
	 * This function may only be called when no OR function has been called on this node before.
	 *
	 * @param verifier The list of verifiers to add. The list may not be null.
	 * @return This instance of the builder to enable command chaining.
	 * @throws IllegalArgumentException Thrown in case this builder node is already an OR node.
	 * @throws NullPointerException Thrown in case the given list is null.
	 */
	fun and(verifier: Collection<CertificateVerifier>): CertificateVerifierBuilder {
		if (orChilds.isEmpty()) {
			andList.addAll(verifier)
			return this
		} else {
			throw IllegalStateException("The CertificateVerifierBuilder already contains OR elements.")
		}
	}

	/**
	 * Creates a new OR node where CertificateVerifier instances can be added.
	 * This function may only be called when no AND function has been called on this node before.
	 *
	 * @return A new instance which is a child of the node this function is called on.
	 * @throws IllegalArgumentException Thrown in case this builder node is already an AND node.
	 */
	fun or(): CertificateVerifierBuilder {
		if (andList.isEmpty()) {
			val cvb = CertificateVerifierBuilder(this)
			orChilds.add(cvb)
			return cvb
		} else {
			throw IllegalStateException("The CertificateVerifierBuilder already contains AND elements.")
		}
	}

	/**
	 * Derives a CertificateVerifier instance based on the configuration of the builder.
	 *
	 * @return The derived CertificateVerifier.
	 */
	fun build(): CertificateVerifier {
		// get back to the root element and start building from there
		return parent?.build() ?: buildInternal()
	}

	private fun buildInternal(): CertificateVerifier {
		// copy and elements so that further modification of the builder does not affect the validation
		val andCopy = Collections.unmodifiableCollection<CertificateVerifier?>(andList)
		// convert OR builder to verifier
		val orCopy = ArrayList<CertificateVerifier>(orChilds.size)
		for (next in orChilds) {
			orCopy.add(next.buildInternal())
		}

		return object : CertificateVerifier {
			@Throws(CertificateVerificationException::class)
			override fun isValid(
				chain: TlsServerCertificate,
				hostname: String,
			) {
				if (!andCopy.isEmpty()) {
					// process each AND check and pass if none failed
					for (cv in andCopy) {
						cv.isValid(chain, hostname)
					}
				} else if (!orCopy.isEmpty()) {
					// process all OR values and fail if none passed
					var noSuccess = true
					for (cv in orCopy) {
						try {
							cv.isValid(chain, hostname)
							// a successful outcome means we passed, so break the loop
							break
						} catch (ex: CertificateVerificationException) {
							noSuccess = false
						}
					}
					if (noSuccess) {
						val msg = "None of the possible validation paths succeeded."
						throw CertificateVerificationException(msg)
					}
				}
			}
		}
	}
}
