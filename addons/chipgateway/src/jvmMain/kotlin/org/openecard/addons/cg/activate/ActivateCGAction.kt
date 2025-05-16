/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.activate

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.ActionInitializationException
import org.openecard.addon.Context
import org.openecard.addon.bind.AppPluginAction
import org.openecard.addon.bind.Attachment
import org.openecard.addon.bind.AuxDataKeys
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.addon.bind.Headers
import org.openecard.addon.bind.RequestBody
import org.openecard.addons.cg.ex.InvalidRedirectUrlException
import org.openecard.addons.cg.ex.InvalidTCTokenElement
import org.openecard.addons.cg.tctoken.TCToken
import org.openecard.common.DynamicContext
import org.openecard.common.ThreadTerminateException
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.Volatile

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class ActivateCGAction : AppPluginAction {
	private var ctx: Context? = null
	private var tokenHandler: TCTokenHandler? = null

	@Throws(ActionInitializationException::class)
	override fun init(aCtx: Context) {
		this.ctx = aCtx
		tokenHandler = ctx?.let { TCTokenHandler(it) }
	}

	override fun destroy(force: Boolean) {
		this.ctx = null
	}

	override fun execute(
		body: RequestBody?,
		parameters: Map<String, String>?,
		headers: Headers?,
		attachments: List<Attachment>?,
		extraParams: Map<String, Any>?,
	): BindingResult {
		var response: BindingResult
		var aquired = false

		try {
			checkMethod(headers)
			val token: TCToken = TCToken.generateToken(parameters!!)

			val cgAction =
				Runnable {
					try {
						tokenHandler!!.handleNoCardActivate(token)

						// run a full GC to free some heap memory
						System.gc()
						System.runFinalization()
						System.gc()
					} catch (ex: ThreadTerminateException) {
						logger.debug(ex) { "Activation task terminated by an interrupt." }
					} catch (ex: RuntimeException) {
						logger.error(ex) { "Unhandled exception in activation process." }
					} finally {
						currentTaskThread = null
						// in some cases an error does not lead to a removal of the dynamic context so remove it here
						DynamicContext.remove()
					}
				}

			// guard thread creation
			MUTEX.acquire()
			aquired = true

			val t: Thread? = currentTaskThread
			t?.let {
				if (token.isForceProcessing) {
					logger.info { "Stopping already running ChipGateway Protocol instance." }
					it.interrupt()
					// wait for other task to complete
					it.join()
				} else {
					val msg = "Another ChipGateway Protocol instance is already running, return status=busy."
					logger.info { msg }
					return BindingResult(BindingResultCode.REDIRECT, msg).apply {
						auxResultData.put(AuxDataKeys.REDIRECT_LOCATION, token.finalizeBusyAddress())
					}
				}
			}

			// perform ChipGateway Protocol in background thread, so that we can return directly
			currentTaskThread =
				Thread(cgAction).apply {
					isDaemon = true
					name = "ChipGateway-Activation-${THREAD_NUM.getAndIncrement()}"
					start()
				}

			// create redirect
			response =
				BindingResult(BindingResultCode.REDIRECT).apply {
					auxResultData.put(AuxDataKeys.REDIRECT_LOCATION, token.finalizeOkAddress())
				}
		} catch (ex: WrongMethodException) {
			logger.warn { ex.message }
			response = BindingResult(BindingResultCode.WRONG_PARAMETER, ex.message)
		} catch (ex: NoMethodException) {
			val msg = "No method given in headers, maybe wrong binging."
			logger.error(ex) { msg }
			response = BindingResult(BindingResultCode.INTERNAL_ERROR, msg)
		} catch (ex: InvalidRedirectUrlException) {
			logger.error(ex) { "Failed to create TCToken." }
			response = ex.bindingResult
		} catch (ex: InvalidTCTokenElement) {
			logger.error(ex) { "Failed to create TCToken." }
			response = ex.bindingResult
		} catch (ex: InterruptedException) {
			val msg = "ChipGateway activation interrupted."
			logger.info { msg }
			response = BindingResult(BindingResultCode.INTERNAL_ERROR, msg)
		} finally {
			if (aquired) {
				MUTEX.release()
			}
		}

		return response
	}

	@Throws(WrongMethodException::class, NoMethodException::class)
	private fun checkMethod(headers: Headers?) {
		val methodHdr = headers?.getFirstHeader(METHOD_HDR)
		if (methodHdr != null) {
			val method = methodHdr.value
			if (method != "GET") {
				val msg = String.format("Wrong method (%s) used to call the plugin action.", method)
				throw WrongMethodException(msg)
			}
		} else {
			throw NoMethodException("No method in headers available, make sure $METHOD_HDR is set.")
		}
	}

	companion object {
		private const val METHOD_HDR = "X-OeC-Method"
		private val THREAD_NUM = AtomicInteger(1)
		private val MUTEX = Semaphore(1, true)

		@Volatile
		private var currentTaskThread: Thread? = null
	}
}
