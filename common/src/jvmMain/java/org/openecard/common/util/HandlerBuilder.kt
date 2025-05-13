/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo
import java.math.BigInteger

/**
 * Builder class for `CardApplicationPathType` and `ConnectionHandleType`.
 * The set methods always emit a copy of the builder with the respective value set. This makes it easy to supply
 * preconfigured builder instances.<br></br>
 * The builder instance is immutable making it safe to use in different threads.
 *
 * @author Tobias Wich
 */
data class HandlerBuilder(
	private val contextHandle: ByteArray? = null,
	private val ifdName: String? = null,
	private val slotIdx: BigInteger? = null,
	private val cardApp: ByteArray? = null,
	private val slotHandle: ByteArray? = null,
	// recognition
	private val cardType: String? = null,
	private val cardIdentifier: ByteArray? = null,
	private val protocolEndpoint: String? = null,
	private val sessionId: String? = null,
	private val binding: String? = null,
	private val protectedAuthPath: Boolean? = null,
) {
	private fun <T : CardApplicationPathType?> buildAppPath(path: T): T {
		path!!.channelHandle = buildChannelHandle()
		path.contextHandle = contextHandle
		path.ifdName = ifdName
		path.slotIndex = slotIdx
		path.cardApplication = cardApp
		return path
	}

	/**
	 * Creates a `CardApplicationPathType` with all available values in the builder.
	 *
	 * @return A `CardApplicationPathType` instance.
	 */
	fun buildAppPath(): CardApplicationPathType {
		val path = buildAppPath(CardApplicationPathType())
		return path
	}

	/**
	 * Creates a `ConnectionHandleType` with all available values in the builder.
	 *
	 * @return A `ConnectionHandleType` instance.
	 */
	fun buildConnectionHandle(): ConnectionHandleType {
		val handle = buildAppPath(ConnectionHandleType())
		handle.slotHandle = slotHandle
		handle.recognitionInfo = buildRecognitionInfo()
		if (protectedAuthPath != null) {
			val slotInfo = ConnectionHandleType.SlotInfo()
			slotInfo.isProtectedAuthPath = protectedAuthPath
			handle.slotInfo = slotInfo
		}
		return handle
	}

	/**
	 * Creates a `RecognitionInfo` if the relevant values are set in the instance.
	 *
	 * @return A `ConnectionHandleType` instance, or `null` if no values are available.
	 */
	fun buildRecognitionInfo(): RecognitionInfo? {
		if (cardType != null) {
			val rInfo = RecognitionInfo()
			rInfo.cardType = cardType
			rInfo.cardIdentifier = cardIdentifier
			return rInfo
		} else {
			return null
		}
	}

	/**
	 * Creates a `ChannelHandleType` if the relevant values are set in the instance.
	 *
	 * @return A `ChannelHandleType` instance, or `null` if no values are available.
	 */
	fun buildChannelHandle(): ChannelHandleType? {
		if (protocolEndpoint != null || sessionId != null || binding != null) {
			val chan = ChannelHandleType()
			chan.protocolTerminationPoint = protocolEndpoint
			chan.sessionIdentifier = sessionId
			chan.binding = binding
			return chan
		} else {
			return null
		}
	}

	fun setContextHandle(contextHandle: ByteArray?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setIfdName(ifdName: String?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setSlotIdx(slotIdx: BigInteger?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setCardApp(cardApp: ByteArray?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setSlotHandle(slotHandle: ByteArray?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setRecognitionInfo(info: RecognitionInfo?): HandlerBuilder {
		if (info != null) {
			val b =
				HandlerBuilder(
					contextHandle,
					ifdName,
					slotIdx,
					cardApp,
					slotHandle,
					info.cardType,
					info.cardIdentifier,
					protocolEndpoint,
					sessionId,
					binding,
					protectedAuthPath,
				)
			return b
		} else {
			return this
		}
	}

	fun setCardType(info: RecognitionInfo?): HandlerBuilder =
		if (info != null) {
			setCardType(info.cardType)
		} else {
			this
		}

	fun setCardType(cardType: String?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setCardIdentifier(info: RecognitionInfo?): HandlerBuilder =
		if (info != null) {
			setCardIdentifier(info.cardIdentifier)
		} else {
			this
		}

	fun setCardIdentifier(cardIdentifier: ByteArray?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setChannelHandle(channel: ChannelHandleType?): HandlerBuilder {
		if (channel != null) {
			val b =
				HandlerBuilder(
					contextHandle,
					ifdName,
					slotIdx,
					cardApp,
					slotHandle,
					cardType,
					cardIdentifier,
					channel.protocolTerminationPoint,
					channel.sessionIdentifier,
					channel.binding,
					protectedAuthPath,
				)
			return b
		} else {
			return this
		}
	}

	fun setProtocolEndpoint(protocolEndpoint: String?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setSessionId(sessionId: String?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setBinding(binding: String?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	fun setProtectedAuthPath(protectedAuthPath: Boolean?): HandlerBuilder {
		val b =
			HandlerBuilder(
				contextHandle,
				ifdName,
				slotIdx,
				cardApp,
				slotHandle,
				cardType,
				cardIdentifier,
				protocolEndpoint,
				sessionId,
				binding,
				protectedAuthPath,
			)
		return b
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as HandlerBuilder

		if (protectedAuthPath != other.protectedAuthPath) return false
		if (!contextHandle.contentEquals(other.contextHandle)) return false
		if (ifdName != other.ifdName) return false
		if (slotIdx != other.slotIdx) return false
		if (!cardApp.contentEquals(other.cardApp)) return false
		if (!slotHandle.contentEquals(other.slotHandle)) return false
		if (cardType != other.cardType) return false
		if (!cardIdentifier.contentEquals(other.cardIdentifier)) return false
		if (protocolEndpoint != other.protocolEndpoint) return false
		if (sessionId != other.sessionId) return false
		if (binding != other.binding) return false

		return true
	}

	override fun hashCode(): Int {
		var result = protectedAuthPath?.hashCode() ?: 0
		result = 31 * result + (contextHandle?.contentHashCode() ?: 0)
		result = 31 * result + (ifdName?.hashCode() ?: 0)
		result = 31 * result + (slotIdx?.hashCode() ?: 0)
		result = 31 * result + (cardApp?.contentHashCode() ?: 0)
		result = 31 * result + (slotHandle?.contentHashCode() ?: 0)
		result = 31 * result + (cardType?.hashCode() ?: 0)
		result = 31 * result + (cardIdentifier?.contentHashCode() ?: 0)
		result = 31 * result + (protocolEndpoint?.hashCode() ?: 0)
		result = 31 * result + (sessionId?.hashCode() ?: 0)
		result = 31 * result + (binding?.hashCode() ?: 0)
		return result
	}

	companion object {
		/**
		 * Create an empty `HandlerBuilder` instance.
		 *
		 * @return Empty instance of a `HandlerBuilder`.
		 */
		@JvmStatic
		fun create(): HandlerBuilder = HandlerBuilder()
	}
}
