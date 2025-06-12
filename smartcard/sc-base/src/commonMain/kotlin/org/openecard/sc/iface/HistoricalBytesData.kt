package org.openecard.sc.iface

import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.common.BitSet
import org.openecard.utils.common.bitSetOf
import org.openecard.utils.common.doIf
import org.openecard.utils.common.toDigitsString
import org.openecard.utils.common.toInt
import org.openecard.utils.common.toNibbles

enum class LifeCycleStatus {
	UNSPECIFIED,
	CREATION,
	INITIALIZATION,
	OPERATIONAL_ACTIVE,
	OPERATIONAL_INACTIVE,
	TERMINATION,
	PROPRIETARY,
	;

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromStatusByte(statusByte: UByte): LifeCycleStatus =
			if (statusByte == 0.toUByte()) {
				UNSPECIFIED
			} else if (statusByte.toUInt() == 0b1u) {
				CREATION
			} else if (statusByte.toUInt() == 0b11u) {
				INITIALIZATION
			} else if ((statusByte.toUInt() and 0b1111_1101u) == 0b0101u) {
				OPERATIONAL_ACTIVE
			} else if ((statusByte.toUInt() and 0b1111_1101u) == 0b0100u) {
				OPERATIONAL_INACTIVE
			} else if ((statusByte.toUInt() and 0b1111_1100u) == 0b1100u) {
				TERMINATION
			} else {
				PROPRIETARY
			}
	}
}

/**
 * Country Indicator according to ISO-7816-4, Sec.8.1.1.2.1.
 */
class CountryIndicator
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val countryDo: TlvPrimitive,
		val countryCode: String,
		val subsequentDataNibbles: UByteArray,
	) {
		// TODO: extract data
		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObjects(dos: List<TlvPrimitive>): CountryIndicator? {
				val issuerTlv = dos.findCompactTlv(0x1u)
				return issuerTlv?.let {
					// A country indicator consists of a country code (three quartets with values from '0' to '9', see ISO 3166-1[1])
					// followed by subsequent data (at least one quartet). The relevant national standardization body shall
					// choose those subsequent data (odd number of quartets).
					if (it.value.size >= 2) {
						val nibbles = it.value.toNibbles()
						val countryCode = nibbles.sliceArray(0 until 3).toDigitsString()
						val subsequentDataNibbles = nibbles.sliceArray(3 until nibbles.size)
						CountryIndicator(it, countryCode, subsequentDataNibbles)
					} else {
						null
					}
				}
			}
		}
	}

/**
 * Issuer Indicator according to ISO-7816-4, Sec.8.1.1.2.1.
 */
class IssuerIndicator(
	private val issuerDo: TlvPrimitive,
	issuerIdentificationNumber: String,
) {
	// TODO: extract data
	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromDataObjects(dos: List<TlvPrimitive>): IssuerIndicator? {
			val issuerTlv = dos.findCompactTlv(0x2u)
			return issuerTlv?.let { tlv ->
				// TODO: extract this data
				// An issuer indicator consists of an issuer identification number (see ISO/IEC 7812-1[3]) possibly
				// followed by subsequent data. The card issuer shall choose those subsequent bytes if any
				// (for encoding, e.g., a Primary Account Number).
				// NOTE: In ISO/IEC 7812-1:1993, an issuer identification number might consist of an odd number of
				// quartets with a value from '0' to '9'. Then it was mapped into a byte string by setting bits 4 to 1
				// of the last byte to 1111.
				val nibbles = tlv.value.toNibbles()
				// TODO: The text is not clear how the subsequent data is separated, so it maybe part of the iin, or not
				// this may also lead to parsing errors
				val issuerIdentificationNumber = nibbles.takeWhile { it.toInt() != 0xF }.toUByteArray().toDigitsString()
				IssuerIndicator(tlv, issuerIdentificationNumber)
			}
		}
	}
}

/**
 * Card Service Data according to ISO-7816-4, Sec.8.1.1.2.3.
 */
class CardServiceData
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val serviceDataDo: TlvPrimitive,
		private val serviceData: BitSet,
	) {
		val selectByFullDf: Boolean by lazy { serviceData[7] }
		val selectByPartialDf: Boolean by lazy { serviceData[6] }
		val hasEfDir: Boolean by lazy { serviceData[5] }
		val hasEfAtr: Boolean by lazy { serviceData[4] }
		val efDirAtrReadBinary by lazy { serviceData[3] && !serviceData[2] && !serviceData[1] }
		val efDirAtrReadRecord by lazy { !serviceData[3] && !serviceData[2] && !serviceData[1] }
		val efDirAtrGetData by lazy { !serviceData[3] && serviceData[2] && !serviceData[1] }
		val hasMf by lazy { !serviceData[0] }

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObjects(dos: List<TlvPrimitive>): CardServiceData? {
				val cardServiceTlv = dos.findCompactTlv(0x3u, 1)
				return cardServiceTlv?.let {
					CardServiceData(it, bitSetOf(*it.value))
				}
			}
		}
	}

/**
 * Initial Access Data according to ISO-7816-4, Sec.8.1.1.2.4.
 */
class InitialAccessData
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val initialAccessDo: TlvPrimitive,
		val apdu: UByteArray,
	) {
		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObjects(dos: List<TlvPrimitive>): InitialAccessData? {
				val tlv = dos.findCompactTlv(0x4u)
				return tlv?.let {
					InitialAccessData(it, it.value)
				}
			}
		}
	}

/**
 * Card Issuers Data according to ISO-7816-4, Sec.8.1.1.2.5.
 */
class CardIssuersData
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val cardIssuersDo: TlvPrimitive,
		val value: UByteArray,
	) {
		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObjects(dos: List<TlvPrimitive>): CardIssuersData? {
				val tlv = dos.findCompactTlv(0x5u)
				return tlv?.let {
					CardIssuersData(it, it.value)
				}
			}
		}
	}

/**
 * Pre Issuing Data according to ISO-7816-4, Sec.8.1.1.2.6.
 */
class PreIssuingData
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val preIssuingDo: TlvPrimitive,
		val value: UByteArray,
	) {
		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObjects(dos: List<TlvPrimitive>): PreIssuingData? {
				val tlv = dos.findCompactTlv(0x6u)
				return tlv?.let {
					PreIssuingData(it, it.value)
				}
			}
		}
	}

/**
 * Card Capabilities according to ISO-7816-4, Sec.8.1.1.2.7.
 */
class CardCapabilities
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val cardCapabilitiesDo: TlvPrimitive,
		private val functionTableBytes: UByteArray,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val selectionMethods: SelectionMethods = SelectionMethods(functionTableBytes[0])

		class SelectionMethods(
			val byte: UByte,
		) {
			@OptIn(ExperimentalUnsignedTypes::class)
			private val functionTable = bitSetOf(byte)
			
			val selectDfByFullName: Boolean by lazy { functionTable[7] }
			val selectDfByPartialName: Boolean by lazy { functionTable[6] }
			val selectDfByPath: Boolean by lazy { functionTable[5] }
			val selectDfByFileId: Boolean by lazy { functionTable[4] }
			val selectDfImplicit: Boolean by lazy { functionTable[3] }
			val supportsShortEf: Boolean by lazy { functionTable[2] }
			val supportsRecordNumber: Boolean by lazy { functionTable[1] }
			val supportsRecordIdentifier: Boolean by lazy { functionTable[0] }
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		val dataCoding: DataCoding? = doIf(functionTableBytes.size >= 2) { DataCoding(functionTableBytes[1]) }

		class DataCoding(
			val byte: UByte,
		) {
			@OptIn(ExperimentalUnsignedTypes::class)
			private val functionTable = bitSetOf(byte)

			val tlvEfs: Boolean by lazy { functionTable[7] }
			val writeOneTime: Boolean by lazy { !functionTable[6] && !functionTable[5] }
			val writeProprietary: Boolean by lazy { !functionTable[6] && functionTable[5] }
			val writeOr: Boolean by lazy { functionTable[6] && !functionTable[5] }
			val writeAnd: Boolean by lazy { functionTable[6] && functionTable[5] }
			val ffValidAsTlvFirstByte: Boolean by lazy { functionTable[4] }

			@OptIn(ExperimentalUnsignedTypes::class)
			val dataUnitsQuartets: Int by lazy {
				val rawValue = byte.toInt() and 0xF
				// calculate 2^rawValue
				1 shl rawValue
			}
			val dataUnitsBytes: Int by lazy {
				if (dataUnitsQuartets.mod(2) == 0) {
					dataUnitsQuartets / 2
				} else {
					// round up, as e.g. 3 quartets need 2 bytes space
					(dataUnitsQuartets + 1) / 2
				}
			}
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		val commandCoding: CommandCoding? = doIf(functionTableBytes.size >= 3) { CommandCoding(functionTableBytes[2]) }

		class CommandCoding(
			val byte: UByte,
		) {
			@OptIn(ExperimentalUnsignedTypes::class)
			private val functionTable = bitSetOf(byte)

			val supportsCommandChaining: Boolean by lazy { functionTable[7] }
			val supportsExtendedLength: Boolean by lazy { functionTable[6] }
			val logicalChannel: LogicalChannelAssignment by lazy {
				val byCard = functionTable[4]
				val byInterface = functionTable[3]
				if (byCard && byInterface) {
					LogicalChannelAssignment.ASSIGN_BY_BOTH
				} else if (byCard) {
					LogicalChannelAssignment.ASSIGN_BY_CARD
				} else if (byInterface) {
					LogicalChannelAssignment.ASSIGN_BY_INTERFACE
				} else {
					LogicalChannelAssignment.NO_LOGICAL_CHANNELS
				}
			}
			val supportsLogicalChannels: Boolean by lazy { logicalChannel != LogicalChannelAssignment.NO_LOGICAL_CHANNELS }
			val maximumLogicalChannels: Int by lazy {
				val y = functionTable[2]
				val z = functionTable[1]
				val t = functionTable[0]
				if (!supportsLogicalChannels) {
					0
				} else if (y && z && t) {
					// 8 or more
					8
				} else {
					(4 * y.toInt()) + (1 * z.toInt()) + t.toInt() + 1
				}
			}
		}

		enum class LogicalChannelAssignment {
			ASSIGN_BY_BOTH,
			ASSIGN_BY_CARD,
			ASSIGN_BY_INTERFACE,
			NO_LOGICAL_CHANNELS,
		}

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObjects(dos: List<TlvPrimitive>): CardCapabilities? {
				val tlv = dos.findCompactTlv(0x7u)
				return tlv?.let {
					CardCapabilities(it, it.value)
				}
			}
		}
	}

class InitialDataStringRecovery
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val dataStringDo: TlvPrimitive,
		val apdu: UByteArray,
	) {
		companion object {
			@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
			fun fromDataObjects(dos: List<TlvPrimitive>): InitialDataStringRecovery? {
				val tlv = dos.findCompactTlv(0x4u)
				return tlv?.let { tlv ->
					val apdu =
						if (tlv.value.size == 1) {
							"00B00000".hexToUByteArray() + tlv.value[0]
						} else if (tlv.value.size == 2) {
							val structureByte = tlv.value[0]
							val structureBits = bitSetOf(structureByte)
							if (structureBits[7]) {
								// transparent
								ubyteArrayOf(0x00u, 0xB0u) + structureByte + 0x00u + tlv.value[1]
							} else {
								// record
								val p2 = (structureByte.toUInt() shl 3) or 0b110u
								ubyteArrayOf(0x00u, 0xB2u, 0x01u) + p2.toUByte() + 0x00u + tlv.value[1]
							}
						} else if (tlv.value.size >= 5) {
							tlv.value
						} else {
							// not a valid apdu, abort
							null
						}

					apdu?.let { InitialDataStringRecovery(tlv, it) }
				}
			}
		}
	}

@OptIn(ExperimentalUnsignedTypes::class)
private fun List<TlvPrimitive>.findCompactTlv(
	shortCode: ULong,
	len: Int? = null,
): TlvPrimitive? {
	val tagNum = 0x40uL or shortCode
	val tlvShortCode = this.filter { it.tag.tagNumWithClass == tagNum }
	return if (len != null) {
		tlvShortCode.find { it.value.size == len }
	} else {
		tlvShortCode.firstOrNull()
	}
}
