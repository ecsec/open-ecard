/****************************************************************************
 * Copyright (C) 2023 ecsec GmbH.
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

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.bouncycastle.tls.CertificateRequest
import org.openecard.bouncycastle.tls.TlsCredentialedSigner
import org.openecard.common.WSHelper
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.crypto.common.sal.TokenFinder

private val LOG = KotlinLogging.logger { }

/**
 * Implementation of CredentialFactory operating on generic crypto SAL DIDs.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
open class SearchingSmartCardCredentialFactory(
	dispatcher: Dispatcher,
	filterAlwaysReadable: Boolean,
	private val evtDispatcher: EventDispatcher,
	private val sessionHandle: ConnectionHandleType,
	private val allowedCardTypes: Set<String>,
) : BaseSmartCardCredentialFactory(dispatcher, filterAlwaysReadable) {
	override var usedHandle: ConnectionHandleType? = null
		protected set

	protected fun isAllowedCardType(cardType: String?): Boolean =
		if (allowedCardTypes.isEmpty()) {
			true
		} else {
			allowedCardTypes.contains(cardType)
		}

	override fun getClientCredentials(cr: CertificateRequest): List<TlsCredentialedSigner> {
		// find a card which can be used to answer the request
		val f = TokenFinder(dispatcher, evtDispatcher, sessionHandle, allowedCardTypes)
		try {
			f.startWatching().use { fw ->
				val card = fw.waitForNext()
				val handle = card.deref()!!
				usedHandle = handle
				return getClientCredentialsForCard(cr, handle)
			}
		} catch (ex: InterruptedException) {
			LOG.warn { "Interrupted while waiting for a card to be inserted, continuing without certificate authentication." }
			return listOf()
		} catch (ex: WSHelper.WSException) {
			LOG.warn { "Error while accessing the smartcard, continuing without certificate authentication." }
			return listOf()
		}
	}
}
