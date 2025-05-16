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

import dev.icerock.moko.resources.format
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.Context
import org.openecard.addon.bind.BindingResult
import org.openecard.addons.cg.ex.ChipGatewayUnknownError
import org.openecard.addons.cg.ex.FatalActivationError
import org.openecard.addons.cg.ex.InvalidRedirectUrlException
import org.openecard.addons.cg.ex.InvalidTCTokenElement
import org.openecard.addons.cg.ex.RedirectionBaseError
import org.openecard.addons.cg.ex.ResultMinor
import org.openecard.addons.cg.impl.ChipGatewayResponse
import org.openecard.addons.cg.impl.ChipGatewayTask
import org.openecard.addons.cg.tctoken.TCToken
import org.openecard.common.ThreadTerminateException
import org.openecard.i18n.I18N
import org.openecard.ws.chipgateway.TerminateType
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.transform.TransformerException

private val logger = KotlinLogging.logger { }

/**
 * Transport binding agnostic TCToken handler. <br></br>
 * This handler supports the following transports:
 *
 *  * ChipGateway
 *
 *
 *
 * This handler supports the following security protocols:
 *
 *  * ChipGateway
 *
 * @param ctx Context containing instances to the core modules.
 *
 * @author Tobias Wich
 */
class TCTokenHandler(
	private val ctx: Context,
) {
	/**
	 * Performs the actual ChipGateway procedure.
	 * Connects the given card, establishes the HTTP channel and talks to the server. Afterwards disconnects the card.
	 *
	 * @param token The TCToken containing the connection parameters.
	 * @return A TCTokenResponse indicating success or failure.
	 * @throws DispatcherException If there was a problem dispatching a request from the server.
	 * @throws ChipGatewayException If there was a transport error.
	 */
	@Throws(InvalidTCTokenElement::class, RedirectionBaseError::class, InvalidRedirectUrlException::class)
	private fun processBinding(token: TCToken): ChipGatewayResponse {
		val response =
			ChipGatewayResponse().apply {
				setToken(token)
			}

		when (token.binding) {
			"http://ws.openecard.org/binding/chipgateway" -> {
				val task = ChipGatewayTask(token, ctx)
				val cgTask = FutureTask<TerminateType?>(task)
				val cgThread = Thread(cgTask, "ChipGateway-" + THREAD_NUM.getAndIncrement())
				cgThread.start()
				// wait for computation to finish
				waitForTask(token, cgTask, cgThread)
			}

			else -> // unknown binding
				throw InvalidTCTokenElement(
					I18N.strings.chipgateway_error_element_value_invalid
						.format("Binding")
						.localized(),
				)
		}

		return response
	}

	/**
	 * Activates the client according to the received TCToken.
	 *
	 * @param token The activation TCToken.
	 * @return The response containing the result of the activation process.
	 */
	fun handleNoCardActivate(token: TCToken): BindingResult {
		if (logger.isDebugEnabled()) {
			try {
				val m = createInstance()
				logger.debug { "TCToken:\n{${m.doc2str(m.marshal(token))}}" }
			} catch (ex: TransformerException) {
				// it's no use
			} catch (ex: WSMarshallerException) {
			}
		}

		try {
			// process binding and follow redirect addresses afterwards
			val response = processBinding(token)
			// fill in values, so it is usuable by the transport module
			response.finishResponse()
			return response
		} catch (ex: RedirectionBaseError) {
			logger.error(ex) { ex.message }
			return ex.bindingResult
		} catch (ex: FatalActivationError) {
			logger.error(ex) { ex.message }
			return ex.bindingResult
		}
	}

	@Throws(RedirectionBaseError::class, InvalidRedirectUrlException::class)
	private fun waitForTask(
		token: TCToken,
		task: Future<*>,
		thread: Thread,
	) {
		try {
			task.get()
		} catch (ex: InterruptedException) {
			task.cancel(true)
			try {
				thread.join()
			} catch (ignore: InterruptedException) {
				// no one cares
			}
			logger.info(ex) { "ChipGateway protocol task cancelled." }
			throw ThreadTerminateException("Waiting for ChipGateway task interrupted.", ex)
		} catch (ex: ExecutionException) {
			logger.error(ex) { ex.message }
			// perform conversion of ExecutionException from the Future to the really expected exceptions
			when (val c = ex.cause) {
				is RedirectionBaseError -> throw c
				else -> {
					throw ChipGatewayUnknownError(
						token.finalizeErrorAddress(ResultMinor.CLIENT_ERROR),
						I18N.strings.chipgateway_error_unknown.localized(),
						ex,
					)
				}
			}
		}
	}

	companion object {
		private val THREAD_NUM = AtomicInteger(1)
	}
}
