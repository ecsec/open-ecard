/****************************************************************************
 * Copyright (C) 2013-2017 ecsec GmbH.
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

import dev.icerock.moko.resources.format
import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse
import org.apache.http.impl.DefaultConnectionReuseStrategy
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestExecutor
import org.openecard.common.WSHelper
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.util.FileUtils.toByteArray
import org.openecard.crypto.tls.auth.BaseSmartCardCredentialFactory
import org.openecard.crypto.tls.auth.PreselectedSmartCardCredentialFactory
import org.openecard.crypto.tls.auth.SearchingSmartCardCredentialFactory
import org.openecard.httpcore.HttpRequestHelper.setDefaultHeader
import org.openecard.httpcore.KHttpUtils.dumpHttpRequest
import org.openecard.httpcore.KHttpUtils.dumpHttpResponse
import org.openecard.httpcore.StreamHttpClientConnection
import org.openecard.i18n.I18N
import java.util.concurrent.Callable

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class HttpGetTask(
	private val dispatcher: Dispatcher,
	private val evtDispatcher: EventDispatcher,
	private val connectionHandle: ConnectionHandleType,
	private val tokenRequest: TCTokenRequest,
) : Callable<StartPAOSResponse?> {
	private val ctxHandle: ByteArray? = connectionHandle.getContextHandle()

	private val credentialFac: BaseSmartCardCredentialFactory?

	init {
		this.credentialFac = makeSmartcardCredentialFactory()
	}

	override fun call(): StartPAOSResponse {
		try {
			this.doRequest()
		} finally {
			// if a handle has been selected in the process, then disconnect it
			val usedHandle = this.usedHandle
			TCTokenHandler.Companion.disconnectHandle(dispatcher, usedHandle)
		}

		// produce a positive result
		val response: StartPAOSResponse =
			WSHelper.makeResponse<Class<StartPAOSResponse>, StartPAOSResponse>(
				iso.std.iso_iec._24727.tech.schema.StartPAOSResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)
		return response
	}

	private val usedHandle: ConnectionHandleType?
		get() {
			return if (credentialFac != null) {
				credentialFac.usedHandle
			} else {
				// special case for early error and when the connection has not been established yet
				connectionHandle
			}
		}

	private fun makeSmartcardCredentialFactory(): BaseSmartCardCredentialFactory {
		if (connectionHandle.getSlotHandle() == null) {
			val cf =
				SearchingSmartCardCredentialFactory(
					dispatcher,
					true,
					evtDispatcher,
					connectionHandle,
					tokenRequest.tCToken.allowedCardType.toSet(),
				)
			return cf
		} else {
			val cf =
				PreselectedSmartCardCredentialFactory(
					dispatcher,
					connectionHandle,
					true,
				)
			return cf
		}
	}

	private fun doRequest() {
		val tlsHandler =
			TlsConnectionHandler(tokenRequest)
		tlsHandler.setSmartCardCredential(credentialFac)
		tlsHandler.setUpClient()

		// connect the tls endpoint and make a get request
		val handler = tlsHandler.createTlsConnection()

		// set up connection to endpoint
		val `in` = handler.inputStream
		val out = handler.outputStream
		val conn =
			StreamHttpClientConnection(`in`, out)

		// prepare HTTP connection
		val ctx: HttpContext = BasicHttpContext()
		val httpexecutor =
			HttpRequestExecutor()
		val reuse =
			DefaultConnectionReuseStrategy()

		// prepare request
		val resource = tlsHandler.getResource()
		val req =
			BasicHttpEntityEnclosingRequest("GET", resource)
		setDefaultHeader(req, tlsHandler.getServerAddress())
		req.setHeader("Accept", "text/html, */*;q=0.8")
		req.setHeader("Accept-Charset", "utf-8, *;q=0.8")
		dumpHttpRequest(
			logger,
			req,
		)

		// send request and receive response
		val response = httpexecutor.execute(req, conn, ctx)
		val statusCode = response.statusLine.statusCode
		conn.receiveResponseEntity(response)
		val entity = response.entity
		val entityData = toByteArray(entity.content)
		dumpHttpResponse(
			logger,
			response,
			entityData,
		)
		conn.close()

		if (statusCode < 200 || statusCode > 299) {
			throw ConnectionError(
				I18N.strings.tr03112_connection_error_invalid_status_code
					.format(statusCode)
					.localized(),
			)
		}
	}
}
