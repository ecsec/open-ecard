/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import iso.std.iso_iec._24727.tech.schema.EmptyResponseDataType
import org.openecard.addon.ActionInitializationException
import org.openecard.addon.Context
import org.openecard.addon.sal.SALProtocolBaseImpl
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.util.Promise
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.annotation.Nonnull

/**
 * Implementation of the EACProtocol using only DIDAuthenticate messages.
 * This class also contains lookup keys for [DynamicContext].
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class EACProtocol : SALProtocolBaseImpl() {
	@Throws(ActionInitializationException::class)
	override fun init(aCtx: Context) {
		addOrderStep(PACEStep(aCtx))
		addOrderStep(TerminalAuthenticationStep(aCtx))
		addOrderStep(ChipAuthenticationStep(aCtx))
	}

	override fun destroy(force: Boolean) {
		LOG.debug("Destroying EAC protocol instance.")
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
		val guiThread = dynCtx.get(TR03112Keys.OPEN_USER_CONSENT_THREAD) as Thread?
		if (guiThread != null) {
			// wait for gui to finish
			try {
				if (force) {
					LOG.debug("Force shutdown of EAC Protocol.")
					guiThread.interrupt()
				}
				LOG.debug("Waiting for EAC GUI to terminate.")
				guiThread.join()
				LOG.debug("EAC GUI terminated.")
			} catch (ex: InterruptedException) {
				// gui thread has its own handling of the shutdown, so interrupt thread and wait
				LOG.debug("Triggering hard shutdown of EAC GUI.")
				guiThread.interrupt()
				// wait again until the GUI is actually gone
				try {
					guiThread.join()
				} catch (ex2: InterruptedException) {
					// ignore as we continue anyway
				}
			}
		}
	}

	override fun isFinished(): Boolean {
		LOG.debug("Checking if EAC protocol is finished.")
		var finished = super.isFinished()
		if (!finished) {
			val ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
			val p: Promise<*> = ctx.getPromise(AUTHENTICATION_DONE)
			if (p.isDelivered) {
				LOG.debug("EAC AUTHENTICATION_DONE promise is delivered.")
				finished = true
				try {
					val failed = !(p.deref() as Boolean)
					if (failed) {
						LOG.debug("EAC AUTHENTICATION_FAILED promise is delivered.")
					} else {
						LOG.debug("EAC AUTHENTICATION_DONE promise is delivered.")
					}
				} catch (ex: InterruptedException) {
					// error would mean don't use the value, so this is ok to ignore
				}
			}
		}
		LOG.debug("EAC authentication finished={}.", finished)
		return finished
	}

	companion object {
		private val LOG: Logger = LoggerFactory.getLogger(EACProtocol::class.java)

		private const val PREFIX = "org.openecard.tr03112.eac."

		@JvmField
		val EAC_DATA: String = PREFIX + "eac_data"

		@JvmField
		val PIN_STATUS: String = PREFIX + "pin_status"
		val IS_NATIVE_PACE: String = PREFIX + "is_native_pace"
		val PACE_MARKER: String = PREFIX + "pace_marker"
		val PACE_EXCEPTION: String = PREFIX + "pace_successful"
		val SLOT_HANDLE: String = PREFIX + "slot_handle"
		val SCHEMA_VALIDATOR: String = PREFIX + "schema_validator"
		val AUTHENTICATION_DONE: String = PREFIX + "authentication_done"

		fun setEmptyResponseData(
			@Nonnull resp: DIDAuthenticateResponse,
		): DIDAuthenticateResponse {
			val d = EmptyResponseDataType()
			d.setProtocol("urn:oid:1.3.162.15480.3.0.14.2")
			resp.setAuthenticationProtocolData(d)
			return resp
		}
	}
}
