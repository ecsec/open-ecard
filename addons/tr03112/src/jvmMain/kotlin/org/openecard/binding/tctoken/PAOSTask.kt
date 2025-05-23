/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.StartPAOS
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse
import org.openecard.common.AppVersion.major
import org.openecard.common.AppVersion.minor
import org.openecard.common.AppVersion.name
import org.openecard.common.AppVersion.patch
import org.openecard.common.ECardConstants
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.DocumentSchemaValidator
import org.openecard.common.util.HandlerUtils
import org.openecard.common.util.Promise
import org.openecard.i18n.I18N
import org.openecard.transport.paos.PAOS
import org.openecard.transport.paos.PAOSException
import java.math.BigInteger
import java.util.concurrent.Callable

/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */

private val LOG = KotlinLogging.logger { }

class PAOSTask(
	private val dispatcher: Dispatcher,
	private val connectionHandle: ConnectionHandleType,
	private val supportedDIDs: List<String>,
	private val tokenRequest: TCTokenRequest,
	private val schemaValidator: Promise<DocumentSchemaValidator>,
) : Callable<StartPAOSResponse> {
	override fun call(): StartPAOSResponse {
		try {
			val tlsHandler = TlsConnectionHandler(tokenRequest)
			tlsHandler.setUpClient()

			val v: DocumentSchemaValidator
			try {
				v = schemaValidator.deref()!!
			} catch (ex: InterruptedException) {
				throw PAOSException(
					msg = I18N.strings.tr03112_paos_exception_interrupted.localized(),
				)
			}

			// Set up PAOS connection
			val p = PAOS(dispatcher, tlsHandler, v)

			// Create StartPAOS message
			val sp = StartPAOS()
			sp.setProfile(ECardConstants.Profile.ECARD_1_1)
			sp.getConnectionHandle().add(this.handleForServer)
			sp.setSessionIdentifier(tlsHandler.getSessionId())

			val ua = StartPAOS.UserAgent()
			ua.setName(name)
			ua.setVersionMajor(BigInteger.valueOf(major.toLong()))
			ua.setVersionMinor(BigInteger.valueOf(minor.toLong()))
			ua.setVersionSubminor(BigInteger.valueOf(patch.toLong()))
			sp.setUserAgent(ua)

			val sv = StartPAOS.SupportedAPIVersions()
			sv.setMajor(ECardConstants.ECARD_API_VERSION_MAJOR)
			sv.setMinor(ECardConstants.ECARD_API_VERSION_MINOR)
			sv.setSubminor(ECardConstants.ECARD_API_VERSION_SUBMINOR)
			sp.getSupportedAPIVersions().add(sv)

			sp.getSupportedDIDProtocols().addAll(supportedDIDs)
			return p.sendStartPAOS(sp)
		} finally {
			try {
				TCTokenHandler.Companion.disconnectHandle(dispatcher, connectionHandle)
			} catch (ex: Exception) {
				LOG.warn(ex) { "Error disconnecting finished handle." }
			}
		}
	}

	private val handleForServer: ConnectionHandleType
		get() {
			val result =
				HandlerUtils.copyHandle(connectionHandle)
			// this is our own extension and servers might not understand it
			result.setSlotInfo(null)
			return result
		}
}
