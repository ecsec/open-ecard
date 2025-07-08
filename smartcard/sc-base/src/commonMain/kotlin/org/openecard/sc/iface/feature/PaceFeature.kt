package org.openecard.sc.iface.feature

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.StatusWordResult
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.toStatusWord
import org.openecard.sc.iface.CommError
import org.openecard.sc.iface.InsufficientBuffer
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.InvalidParameter
import org.openecard.sc.iface.InvalidValue
import org.openecard.sc.iface.NoService
import org.openecard.sc.iface.NotTransacted
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.RemovedCard
import org.openecard.sc.iface.ResetCard
import org.openecard.sc.iface.feature.PaceCapability.Companion.toPaceCapabilities
import org.openecard.utils.common.BitSet
import org.openecard.utils.common.bitSetOf
import org.openecard.utils.common.toUByte
import org.openecard.utils.common.toUByteArray
import org.openecard.utils.common.toUInt
import org.openecard.utils.common.toUShort
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable
import kotlin.coroutines.cancellation.CancellationException

interface PaceFeature : Feature {
	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoService::class,
		NotTransacted::class,
		ReaderUnavailable::class,
		CommError::class,
		ResetCard::class,
		RemovedCard::class,
		PaceError::class,
	)
	fun getPaceCapabilities(): Set<PaceCapability>

	@OptIn(ExperimentalUnsignedTypes::class)
	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoService::class,
		NotTransacted::class,
		ReaderUnavailable::class,
		CommError::class,
		ResetCard::class,
		RemovedCard::class,
		PaceError::class,
		CancellationException::class,
	)
	suspend fun establishChannel(req: PaceEstablishChannelRequest): PaceEstablishChannelResponse

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoService::class,
		NotTransacted::class,
		ReaderUnavailable::class,
		CommError::class,
		ResetCard::class,
		RemovedCard::class,
		PaceError::class,
	)
	fun destroyChannel()
}

class PaceError(
	val error: PaceResultCode,
	val swCode: StatusWordResult?,
	val securityError: SecurityCommandFailure? =
		swCode?.let {
			SecurityCommandFailure(ApduProcessingError(it, "PACE execution error"))
		},
) : Exception("${error.description}${if (swCode != null) ": $swCode" else ""}")

enum class PaceResultCode(
	val code: UInt,
	val description: String,
	val hasSwCode: Boolean = false,
) {
	NO_ERROR(0u, "No error"),

	// Errors in input data
	INCONSISTENT_LENGTH(0xD0000001u, "Inconsistent lengths in input"),
	UNEXPECTED_DATA(0xD0000002u, "Unexpected data in input"),
	UNEXPECTED_COMBINATION_OF_DATA(0xD0000003u, "Unexpected combination of data in input"),

	// Errors during protocol execution
	TLV_RESPONSE_SYNTAX(0xE0000001u, "Syntax error in TLV response"),
	UNEXPECTED_TLV_RESPONSE_OBJECT(0xE0000002u, "Unexpected or missing object in TLV response"),
	UNKOWN_PIN(0xE0000003u, "Unknown PIN-ID"),
	WRONG_AUTH_TOKEN(0xE0000006u, "Wrong Authentication Token"),

	// Response APDU of the card reports error (status code SW1SW2)
	SELECT_EFCA_ERROR(0xF0000000u, "Select EF.CardAccess", true),
	READ_EFCA_ERROR(0xF0010000u, "Read Binary EF.CardAccess", true),
	MSE_SET_AT_ERROR(0xF0020000u, "MSE: Set AT", true),
	GA1_ERROR(0xF0030000u, "General Authenticate Step 1", true),
	GA2_ERROR(0xF0040000u, "General Authenticate Step 2", true),
	GA3_ERROR(0xF0050000u, "General Authenticate Step 3", true),
	GA4_ERROR(0xF0060000u, "General Authenticate Step 4", true),

	// Others
	COM_ABORT(0xF0100001u, "Communication abort (e.g. card removed during protocol)"),
	NO_CARD(0xF0100002u, "No card"),
	ABORT(0xF0200001u, "Abort"),
	TIMEOUT(0xF0200002u, "Timeout"),
	;

	fun matchesCode(code: UInt): Boolean =
		if (hasSwCode) {
			this.code == (code and 0xFFFF.toUInt().inv())
		} else {
			this.code == code
		}

	fun getSwCode(code: UInt): UShort? =
		if (hasSwCode) {
			// this cuts off the higher bytes
			code.toUShort()
		} else {
			null
		}

	companion object {
		fun findForCode(code: UInt): PaceResultCode? = entries.find { it.matchesCode(code) }
	}
}

@OptIn(ExperimentalStdlibApi::class)
fun UInt.toPaceError(): PaceError? {
	val errorCode =
		PaceResultCode.findForCode(this)
			?: throw IllegalStateException("PACE execution returned an unknown error code 0x${this.toHexString()}")
	return when (errorCode) {
		PaceResultCode.NO_ERROR -> null
		else -> PaceError(errorCode, errorCode.getSwCode(this)?.toStatusWord())
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
@Throws(PaceError::class)
private fun UByteArray.getDataFromPaceResponse(): UByteArray {
	val errorCode = this.toUInt(0, false)
	errorCode.toPaceError()?.let { throw it }

	val len = this.toUShort(4, false)
	val data = this.sliceArray(6 until 6 + len.toInt())
	return data
}

enum class PaceFunction(
	val code: UByte,
) {
	GET_READER_CAPABILITIES(0x1u),
	ESTABLISH_CHANNEL(0x2u),
	DESTROY_CHANNEL(0x3u),
}

enum class PaceCapability(
	val code: UByte,
	val bitPos: Int,
) {
	QES(0x10u, 4),
	GERMAN_EID(0x20u, 5),
	GENERIC_PACE(0x40u, 6),
	DESTROY_CHANNEL(0x80u, 7),
	;

	companion object {
		fun BitSet.toPaceCapabilities(): Set<PaceCapability> {
			val result = mutableSetOf<PaceCapability>()
			PaceCapability.entries.forEach {
				if (this[it.bitPos]) {
					result.add(it)
				}
			}
			return result
		}
	}
}

enum class PacePinId(
	val code: UByte,
) {
	MRZ(0x1u),
	CAN(0x2u),
	PIN(0x3u),
	PUK(0x4u),
}

object PaceGetReaderCapabilitiesRequest {
	@OptIn(ExperimentalUnsignedTypes::class)
	val bytes: UByteArray by lazy {
		buildList<UByte> {
			add(PaceFunction.GET_READER_CAPABILITIES.code)
			addAll(0x0u.toUShort().toUByteArray())
		}.toUByteArray()
	}
}

data class GetReaderCapabilitiesResponse(
	val capabilities: Set<PaceCapability>,
) {
	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(PaceError::class)
		fun fromPaceResponse(response: UByteArray): GetReaderCapabilitiesResponse {
			val data = response.getDataFromPaceResponse()

			val capsBits =
				if (data.size > 1) {
					// first field is length
					val bitsLen = data.toUByte(0).toInt()
					val bitsData = data.sliceArray(1 until (1 + bitsLen))
					bitSetOf(*bitsData)
				} else {
					bitSetOf(*data)
				}

			val caps = capsBits.toPaceCapabilities()
			return GetReaderCapabilitiesResponse(caps)
		}
	}
}

data class PaceEstablishChannelRequest(
	val pinId: PacePinId,
	/**
	 * The following elements are only present if the execution of PACE is to be followed by an
	 * execution of Terminal Authentication Version 2 as defined in [TR-03110].
	 */
	val chat: PrintableUByteArray?,
	/**
	 * If the PIN to be used is not secret (e.g. printed on the card/stored in the host), it may be
	 * delivered by the host to the IFD in the following elements.
	 * A suitable command filter should be employed by the IFD to refuse delivery of secret PINs by the host.
	 */
	val pin: String?,
	/**
	 * Certificate Extensions
	 *
	 * This data object is REQUIRED for PACE if Terminal Authentication version 2 shall be used after PACE and the
	 * ICC supports Authorization Extensions.
	 * In this case, it MUST encapsulate a sequence of Authorization Extensions.
	 */
	val certDesc: PrintableUByteArray?,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	fun bytes(capabilities: Set<PaceCapability>): UByteArray {
		val establishChannelData =
			buildList<UByte> {
				add(pinId.code)
				(chat?.v ?: ubyteArrayOf()).let { chat ->
					add(chat.size.toUByte())
					addAll(chat)
				}
				(pin?.encodeToByteArray()?.toUByteArray() ?: ubyteArrayOf()).let { pin ->
					add(pin.size.toUByte())
					addAll(pin)
				}
				// only add when the terminal is German_EID or QES capable, thus doing TA
				if (capabilities.any { it in setOf(PaceCapability.GERMAN_EID, PaceCapability.QES) }) {
					(certDesc?.v ?: ubyteArrayOf()).let { certDesc ->
						addAll(certDesc.size.toUShort().toUByteArray(false))
						addAll(certDesc)
					}
				}
			}.toUByteArray()
		val estChanLen = establishChannelData.size.toUShort().toUByteArray(false)

		return PaceFunction.ESTABLISH_CHANNEL.code.toUByteArray() + estChanLen + establishChannelData
	}
}

data class PaceEstablishChannelResponse(
	val mseStatus: StatusWordResult,
	val efCardAccess: PrintableUByteArray,
	val carCurr: PrintableUByteArray?,
	val carPrev: PrintableUByteArray?,
	val idIcc: PrintableUByteArray?,
) {
	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(PaceError::class)
		fun fromPaceResponse(response: UByteArray): PaceEstablishChannelResponse {
			val data = response.getDataFromPaceResponse()
			var offset = 0

			val status = data.toUShort(offset, false)
			offset += 2

			val efCaLen = data.toUShort(offset, false).toInt()
			val efCa = data.sliceArray((offset + 2) until (offset + 2 + efCaLen))
			offset += 2 + efCaLen

			val carCurrLen = data.toUByte(offset).toInt()
			val carCurr =
				if (carCurrLen > 0) {
					data.sliceArray((offset + 1) until (offset + 1 + carCurrLen))
				} else {
					null
				}
			offset += 1 + carCurrLen

			val carPrevLen = data.toUByte(offset).toInt()
			val carPrev =
				if (carPrevLen > 0) {
					data.sliceArray((offset + 1) until (offset + 1 + carPrevLen))
				} else {
					null
				}
			offset += 1 + carPrevLen

			val idIccLen = data.toUShort(offset, false).toInt()
			offset += 2
			val idIcc =
				if (idIccLen > 0) {
					data.sliceArray(offset until (offset + idIccLen))
				} else {
					null
				}

			return PaceEstablishChannelResponse(
				status.toStatusWord(),
				efCa.toPrintable(),
				carCurr?.toPrintable(),
				carPrev?.toPrintable(),
				idIcc?.toPrintable(),
			)
		}
	}
}

object PaceDestroyChannelRequest {
	@OptIn(ExperimentalUnsignedTypes::class)
	val bytes: UByteArray by lazy {
		buildList<UByte> {
			add(PaceFunction.DESTROY_CHANNEL.code)
			addAll(0x0u.toUShort().toUByteArray(false))
		}.toUByteArray()
	}
}

object PaceDestroyChannelResponse {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Throws(PaceError::class)
	fun fromPaceResponse(response: UByteArray): PaceDestroyChannelResponse {
		// parse response and raise error if necessary
		response.getDataFromPaceResponse()
		return PaceDestroyChannelResponse
	}
}
