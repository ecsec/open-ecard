package org.openecard.sc.iface

import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.sc.tlv.toTlvCompact
import org.openecard.utils.common.toUShort

class HistoricalBytes
	@OptIn(ExperimentalUnsignedTypes::class)
	internal constructor(
		val bytes: UByteArray,
		/**
		 * If the category indicator byte is set to '00', '10' or '8X', then Table 83 summarizes the format of the
		 * historical bytes. Any other value indicates a proprietary format.
		 */
		val categoryIndicator: UByte,
		val dirReference: UByteArray? = null,
		val dataObjects: List<TlvPrimitive> = listOf(),
		val cardLifecycleStatus: UByte = 0x00u,
		val sw: UShort = 0x000u,
	) {
		val lifeCycleStatus by lazy { LifeCycleStatus.fromStatusByte(cardLifecycleStatus) }

		val countryIndicator by lazy { CountryIndicator.fromDataObjects(dataObjects) }
		val issuerIndicator by lazy { IssuerIndicator.fromDataObjects(dataObjects) }
		val applicationIdentifier by lazy { ApplicationIdentifier.fromDataObjects(dataObjects) }
		val cardServiceData by lazy { CardServiceData.fromDataObjects(dataObjects) }
		val initialAccessData by lazy { InitialAccessData.fromDataObjects(dataObjects) }
		val cardIssuersData by lazy { CardIssuersData.fromDataObjects(dataObjects) }
		val preIssuingData by lazy { PreIssuingData.fromDataObjects(dataObjects) }
		val cardCapabilities by lazy { CardCapabilities.fromDataObjects(dataObjects) }

		val initialDataStringRecovery by lazy { InitialDataStringRecovery.fromDataObjects(dataObjects) }
	}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toHistoricalBytes(): HistoricalBytes? {
	if (this.isEmpty()) {
		return null
	}

	var idx = 0
	val categoryIndicator = this[idx++]
	
	when (categoryIndicator.toInt()) {
		0x00 -> {
			val status = this.sliceArray(this.size - 3 until this.size)
			val cardLifecycleStatus = status[0]
			val sw = status.toUShort(1)
			val dataObjects =
				if (this.size - idx > 3) {
					val (tlv) = this.sliceArray(idx until size - 3).toTlvCompact()
					tlv.asList().filterIsInstance<TlvPrimitive>()
				} else {
					listOf()
				}
			return HistoricalBytes(
				this,
				categoryIndicator,
				dataObjects = dataObjects,
				cardLifecycleStatus = cardLifecycleStatus,
				sw = sw,
			)
		}

		0x10 -> {
			val dirRef = this.sliceArray(idx until this.size)
			return HistoricalBytes(this, categoryIndicator, dirReference = dirRef)
		}

		0x80 -> {
			val (tlv) = this.sliceArray(idx until size).toTlvCompact()
			val dataObjects = tlv.asList().filterIsInstance<TlvPrimitive>()
			val statusObject = dataObjects.last()
			checkNotNull(statusObject)

			when (statusObject.tag.tagNumWithClass) {
				0x48uL -> {
					when (statusObject.value.size) {
						1 -> {
							val lcs = statusObject.value[0]
							return HistoricalBytes(
								this,
								categoryIndicator,
								dataObjects = dataObjects,
								cardLifecycleStatus = lcs,
							)
						}

						2 -> {
							val sw = statusObject.value.toUShort(0)
							return HistoricalBytes(this, categoryIndicator, dataObjects = dataObjects, sw = sw)
						}

						3 -> {
							val lcs = statusObject.value[0]
							val sw = statusObject.value.toUShort(1)
							return HistoricalBytes(
								this,
								categoryIndicator,
								dataObjects = dataObjects,
								sw = sw,
								cardLifecycleStatus = lcs,
							)
						}

						else -> {
							throw IllegalArgumentException("Historical bytes are missing a required status object.")
						}
					}
				}

				else -> {
					return HistoricalBytes(
						this,
						categoryIndicator,
						dataObjects = dataObjects,
					)
				}
			}
		}

		else -> {
			// future use
			return HistoricalBytes(this, categoryIndicator)
		}
	}
}
