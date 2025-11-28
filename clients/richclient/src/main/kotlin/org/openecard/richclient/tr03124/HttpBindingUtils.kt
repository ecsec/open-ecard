/*
 * Copyright (C) 2025 ecsec GmbH.
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

package org.openecard.richclient.tr03124

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingCall
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.i18n.I18N

private val log = KotlinLogging.logger { }

internal suspend fun BindingResponse.toKtorResponse(call: RoutingCall) {
	val httpStatus = HttpStatusCode.fromValue(status)
	when (this) {
		is BindingResponse.RedirectResponse -> {
			if (status in 300..<400) {
				call.response.headers.append(HttpHeaders.Location, redirectUrl)
				call.respond(httpStatus)
			} else {
				log.error { "Invalid redirect status code returned from application logic" }
				call.respondText(
					text = I18N.strings.tr03112_error_internal.localized(),
					status = httpStatus,
				)
			}
		}

		is BindingResponse.ReferencedContentResponse -> {
			when (payload) {
				BindingResponse.ContentCode.NO_SUITABLE_ACTIVATION_PARAMETERS -> {
					call.respondText(
						text = I18N.strings.tr03112_missing_activation_parameter_exception_no_suitable_parameters.localized(),
						status = httpStatus,
					)
				}

				BindingResponse.ContentCode.TC_TOKEN_RETRIEVAL_ERROR -> {
					call.respondText(
						text = I18N.strings.tr03112_tctoken_retrieval_exception.localized(),
						status = httpStatus,
					)
				}

				BindingResponse.ContentCode.COMMUNICATION_ERROR -> {
					call.respondText(
						text = I18N.strings.tr03112_communication_error.localized(),
						status = httpStatus,
					)
				}

				BindingResponse.ContentCode.OTHER_PROCESS_RUNNING -> {
					call.respondText(
						text = I18N.strings.tr03112_auth_process_running.localized(),
						status = httpStatus,
					)
				}

				BindingResponse.ContentCode.NO_ACCEPTABLE_FORMAT -> {
					call.respondText(
						text = I18N.strings.http_406.localized(),
						status = httpStatus,
					)
				}

				BindingResponse.ContentCode.INTERNAL_ERROR -> {
					call.respondText(
						text = I18N.strings.tr03112_error_internal.localized(),
						status = httpStatus,
					)
				}
			}
		}

		is BindingResponse.ContentResponse -> {
			val ct = ContentType.parse(contentType)
			call.respondBytes(status = httpStatus, contentType = ct) { this.payload }
		}

		is BindingResponse.NoContent -> {
			call.response.status(httpStatus)
		}
	}
}
