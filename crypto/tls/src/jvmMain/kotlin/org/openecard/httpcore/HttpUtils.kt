/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

import io.github.oshai.kotlinlogging.KLogger
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.slf4j.Logger
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Utility functions for Apache HTTP core.
 *
 * @author Tobias Wich
 */
object HttpUtils {
	/**
	 * Dump the given HTTP request and log it with the given logger instance.
	 *
	 * @see .dumpHttpRequest
	 * @param logger Logger to dump HTTP request to.
	 * @param req Request to dump.
	 */
	@JvmStatic
	fun dumpHttpRequest(
		logger: Logger,
		req: HttpRequest,
	) {
		dumpHttpRequest(logger, null, req)
	}

	/**
	 * Dump the given HTTP request and log it with the given logger instance.
	 * An optional message can be given wich will be printed in the head of the log entry to define the context of the
	 * message. The request message is not modified by this method.
	 *
	 * @param logger Logger to dump HTTP request to.
	 * @param msg Message qualifying the context of the request.
	 * @param req Request to dump.
	 */
	@JvmStatic
	fun dumpHttpRequest(
		logger: Logger,
		msg: String?,
		req: HttpRequest,
	) {
		if (logger.isDebugEnabled) {
			val w = StringWriter()
			val pw = PrintWriter(w)

			pw.print("HTTP Request")
			if (msg != null) {
				pw.format(" (%s)", msg)
			}
			pw.println(":")
			val rl = req.requestLine
			pw.format("  %s %s %s%n", rl.method, rl.uri, rl.protocolVersion.toString())
			for (h in req.allHeaders) {
				pw.format("  %s: %s%n", h.name, h.value)
			}
			pw.flush()

			logger.debug(w.toString())
		}
	}

	/**
	 * Dump the given HTTP response and log it with the given logger instance.
	 * The response message is not modifyed by the method. If the data contained in the message should be printed, it
	 * must be extracted seperately and provided in the respective parameter.
	 *
	 * @param logger Logger to dump HTTP request to.
	 * @param res Response to dump.
	 * @param entityData Response data to dump if not null.
	 */
	@JvmStatic
	fun dumpHttpResponse(
		logger: Logger,
		res: HttpResponse,
		entityData: String?,
	) {
		if (logger.isDebugEnabled) {
			val w = StringWriter()
			val pw = PrintWriter(w)

			pw.println("HTTP Response:")
			val sl = res.statusLine
			pw.format("  %s %d %s%n", sl.protocolVersion.toString(), sl.statusCode, sl.reasonPhrase)
			for (h in res.allHeaders) {
				pw.format("  %s: %s%n", h.name, h.value)
			}
			if (entityData != null) {
				pw.print(entityData)
			}
			pw.println()
			pw.flush()

			logger.debug(w.toString())
		}
	}

	/**
	 * Dump the given HTTP response and log it with the given logger instance.
	 * The response message is not modifyed by the method. If the data contained in the message should be printed, it
	 * must be extracted seperately and provided in the respective parameter.
	 *
	 * @param logger Logger to dump HTTP request to.
	 * @param res Response to dump.
	 * @param entityData Response data to dump if not null.
	 */
	@JvmStatic
	fun dumpHttpResponse(
		logger: Logger,
		res: HttpResponse,
		entityData: ByteArray?,
	) {
		dumpHttpResponse(logger, res, if (entityData != null) String(entityData) else null)
	}

	@JvmStatic
	fun dumpHttpResponse(
		logger: Logger,
		res: HttpResponse,
	) {
		dumpHttpResponse(logger, res, null as String?)
	}
}

/**
 * Utility functions for Apache HTTP core.
 *
 * @author Tobias Wich
 */
object KHttpUtils {
	/**
	 * Dump the given HTTP request and log it with the given logger instance.
	 *
	 * @see .dumpHttpRequest
	 * @param logger Logger to dump HTTP request to.
	 * @param req Request to dump.
	 */
	fun dumpHttpRequest(
		logger: KLogger,
		req: HttpRequest,
	) {
		dumpHttpRequest(logger, null, req)
	}

	/**
	 * Dump the given HTTP request and log it with the given logger instance.
	 * An optional message can be given wich will be printed in the head of the log entry to define the context of the
	 * message. The request message is not modified by this method.
	 *
	 * @param logger Logger to dump HTTP request to.
	 * @param msg Message qualifying the context of the request.
	 * @param req Request to dump.
	 */
	fun dumpHttpRequest(
		logger: KLogger,
		msg: String?,
		req: HttpRequest,
	) {
		if (logger.isDebugEnabled()) {
			val w = StringWriter()
			val pw = PrintWriter(w)

			pw.print("HTTP Request")
			if (msg != null) {
				pw.format(" (%s)", msg)
			}
			pw.println(":")
			val rl = req.requestLine
			pw.format("  %s %s %s%n", rl.method, rl.uri, rl.protocolVersion.toString())
			for (h in req.allHeaders) {
				pw.format("  %s: %s%n", h.name, h.value)
			}
			pw.flush()

			logger.debug { w.toString() }
		}
	}

	/**
	 * Dump the given HTTP response and log it with the given logger instance.
	 * The response message is not modifyed by the method. If the data contained in the message should be printed, it
	 * must be extracted seperately and provided in the respective parameter.
	 *
	 * @param logger Logger to dump HTTP request to.
	 * @param res Response to dump.
	 * @param entityData Response data to dump if not null.
	 */
	fun dumpHttpResponse(
		logger: KLogger,
		res: HttpResponse,
		entityData: String?,
	) {
		if (logger.isDebugEnabled()) {
			val w = StringWriter()
			val pw = PrintWriter(w)

			pw.println("HTTP Response:")
			val sl = res.statusLine
			pw.format("  %s %d %s%n", sl.protocolVersion.toString(), sl.statusCode, sl.reasonPhrase)
			for (h in res.allHeaders) {
				pw.format("  %s: %s%n", h.name, h.value)
			}
			if (entityData != null) {
				pw.print(entityData)
			}
			pw.println()
			pw.flush()

			logger.debug { w.toString() }
		}
	}

	/**
	 * Dump the given HTTP response and log it with the given logger instance.
	 * The response message is not modifyed by the method. If the data contained in the message should be printed, it
	 * must be extracted seperately and provided in the respective parameter.
	 *
	 * @param logger Logger to dump HTTP request to.
	 * @param res Response to dump.
	 * @param entityData Response data to dump if not null.
	 */
	fun dumpHttpResponse(
		logger: KLogger,
		res: HttpResponse,
		entityData: ByteArray?,
	) {
		dumpHttpResponse(logger, res, if (entityData != null) String(entityData) else null)
	}

	fun dumpHttpResponse(
		logger: KLogger,
		res: HttpResponse,
	) {
		dumpHttpResponse(logger, res, null as String?)
	}
}
