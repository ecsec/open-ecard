/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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
package org.openecard.common.util

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo
import iso.std.iso_iec._24727.tech.schema.PathSecurityType
import java.util.Formatter
import javax.xml.datatype.XMLGregorianCalendar
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

private val LOG = KotlinLogging.logger { }

/**
 * Utility class for `CardApplicationPathType` and `ConnectionHandleType`.
 *
 * @author Tobias Wich
 */
class HandlerUtils {
	fun createBuilder(): HandlerBuilder = HandlerBuilder.create()

	companion object {
		// TODO: use builder to copy handles
		@JvmStatic
		fun copyHandle(handle: ConnectionHandleType): ConnectionHandleType {
			val result = ConnectionHandleType()
			copyPath(result, handle)
			result.slotHandle = ByteUtils.clone(handle.slotHandle)
			result.recognitionInfo = copyRecognition(handle.recognitionInfo)
			result.slotInfo = copySlotInfo(handle.slotInfo)
			return result
		}

		@JvmStatic
		fun copyPath(handle: CardApplicationPathType): CardApplicationPathType {
			val result = CardApplicationPathType()
			copyPath(result, handle)
			return result
		}

		private fun copyPath(
			out: CardApplicationPathType,
			`in`: CardApplicationPathType,
		) {
			out.cardApplication = ByteUtils.clone(`in`.cardApplication)
			out.channelHandle = copyChannel(`in`.channelHandle)
			out.contextHandle = ByteUtils.clone(`in`.contextHandle)
			out.ifdName = `in`.ifdName
			out.slotIndex = `in`.slotIndex // TODO: copy bigint
		}

		private fun copyChannel(handle: ChannelHandleType?): ChannelHandleType? {
			if (handle == null) {
				return null
			}
			val result = ChannelHandleType()
			result.binding = handle.binding
			result.pathSecurity = copyPathSec(handle.pathSecurity)
			result.protocolTerminationPoint = handle.protocolTerminationPoint
			result.sessionIdentifier = handle.sessionIdentifier
			return result
		}

		private fun copyRecognition(rec: RecognitionInfo?): RecognitionInfo? {
			if (rec == null) {
				return null
			}
			val result = RecognitionInfo()
			if (rec.captureTime != null) {
				result.captureTime = rec.captureTime.clone() as XMLGregorianCalendar
			}
			result.cardIdentifier = ByteUtils.clone(rec.cardIdentifier)
			result.cardType = rec.cardType
			return result
		}

		private fun copyPathSec(sec: PathSecurityType?): PathSecurityType? {
			if (sec == null) {
				return null
			}
			val result = PathSecurityType()
			result.parameters = sec.parameters // TODO: copy depending on actual content
			result.protocol = sec.protocol
			return result
		}

		private fun copySlotInfo(slotInfo: ConnectionHandleType.SlotInfo?): ConnectionHandleType.SlotInfo? {
			if (slotInfo == null) {
				return null
			}

			val result = ConnectionHandleType.SlotInfo()
			result.isProtectedAuthPath = slotInfo.isProtectedAuthPath
			return result
		}

		@JvmStatic
		fun extractHandle(obj: Any): ConnectionHandleType? {
			// SAL calls
			val handle =
				getMember(
					obj,
					"getConnectionHandle",
					ConnectionHandleType::class,
				)
			if (handle != null) {
				LOG.debug { "${"Found ConnectionHandle in object of type {}."} ${obj.javaClass.simpleName}" }
				return handle
			}

			// IFD calls with context handle
			val ctxHandle =
				getMember(
					obj,
					"getContextHandle",
					ByteArray::class,
				)
			if (ctxHandle != null) {
				LOG.debug { "${"Found ContextHandle in object of type {}."} ${obj.javaClass.simpleName}" }
				val ifdName = getMember(obj, "getIFDName", String::class)
				val sessionId =
					getMember(
						obj,
						"getSessionIdentifier",
						String::class,
					)
				return HandlerBuilder.Companion
					.create()
					.setContextHandle(ctxHandle)
					.setIfdName(ifdName)
					.setSessionId(sessionId)
					.buildConnectionHandle()
			}

			// IFD calls with slot handle
			val slotHandle =
				getMember(
					obj,
					"getSlotHandle",
					ByteArray::class,
				)
			if (slotHandle != null) {
				LOG.debug { "${"Found SlotHandle in object of type {}."} ${obj.javaClass.simpleName}" }
				return HandlerBuilder.Companion
					.create()
					.setSlotHandle(slotHandle)
					.buildConnectionHandle()
			}

			// no handle could be determined
			return null
		}

		private fun <T : Any> getMember(
			obj: Any,
			methodName: String,
			memberType: KClass<T>,
		): T? {
			val getter =
				obj::class.memberFunctions.firstOrNull {
					it.name == methodName
				}
			if (getter != null) {
				return getter.call(obj) as? T
			} else {
				return null
			}
		}

		@JvmStatic
		fun print(handle: ConnectionHandleType): String = print(handle, "", "  ")

		@JvmStatic
		fun print(
			handle: ConnectionHandleType,
			prefix: String,
			prefixIncrement: String,
		): String {
			val p1 = prefix
			val p2 = p1 + prefixIncrement
			val p3 = p2 + prefixIncrement
			val f = Formatter()
			f.format("%sConnectionHandle {%n", p1)
			f.format("%sctx=%s%n", p2, ByteUtils.toHexString(handle.contextHandle))
			f.format("%sifd=%s%n", p2, handle.ifdName)
			f.format("%sidx=%s%n", p2, handle.slotIndex)
			f.format("%sslot=%s%n", p2, ByteUtils.toHexString(handle.slotHandle))
			f.format("%sapp=%s%n", p2, ByteUtils.toHexString(handle.cardApplication))
			val ri = handle.recognitionInfo
			if (ri != null) {
				f.format("%sRecognition {%n", p2)
				f.format("%styp=%s%n", p3, ri.cardType)
				f.format("%sident=%s%n", p3, ByteUtils.toHexString(ri.cardIdentifier))
				f.format("%s}%n", p2)
			}
			f.format("%s}", p1)
			return f.toString()
		}
	}
}
