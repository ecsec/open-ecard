package org.openecard.sc.iface

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.utils.common.BitSet
import org.openecard.utils.common.bitSetOf
import org.openecard.utils.serialization.toPrintable

private val log = KotlinLogging.logger { }

class Atr
	@OptIn(ExperimentalUnsignedTypes::class)
	internal constructor(
		val bytes: UByteArray,
		val t0: UByte,
		val ts: UByte,
		val tas: List<UByte?>,
		val tbs: List<UByte?>,
		val tcs: List<UByte?>,
		val tds: List<UByte?>,
		val historicalBytes: HistoricalBytes?,
		val tck: UByte?,
	) {
		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromHistoricalBytes(histBytesTmp: UByteArray): Atr =
				buildList {
					// T0
					add((histBytesTmp.size and 0xF).toUByte())
					// ISO14443A: The historical bytes from ATS response.
					// ISO14443B: 1-4=Application Data from ATQB, 5-7=Protocol Info Byte from ATQB, 8=Higher nibble = MBLI from ATTRIB command Lower nibble (RFU) = 0
					// TODO: check that the HiLayerResponse matches the requirements for ISO14443B
					addAll(histBytesTmp)
				}.toUByteArray().toAtr(0x3Bu)
		}
	}

/**
 * Parse the ATR.
 *
 * In case the ATR does not come with a TS byte, it can be simulated.
 * Common values would be `3B` (direct convention) or `3F` (indirect convention).
 * @param simulateTs Use to simulate TS in case the input is missing this byte.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toAtr(simulateTs: UByte? = null): Atr {
	var idx = 0

	val ts =
		simulateTs ?: this[idx++]
	val t0 = this[idx++]
	var numInterface: BitSet? = bitSetOf((t0.toInt() shr 4).toUByte())
	val numHistorical = t0.toInt() and 0x0F

	val tas = mutableListOf<UByte?>()
	val tbs = mutableListOf<UByte?>()
	val tcs = mutableListOf<UByte?>()
	val tds = mutableListOf<UByte?>()
	while (numInterface != null) {
		// TAi
		if (numInterface[0]) {
			tas.add(this[idx++])
		} else {
			tas.add(null)
		}
		// TBi
		if (numInterface[1]) {
			tbs.add(this[idx++])
		} else {
			tbs.add(null)
		}
		// TCi
		if (numInterface[2]) {
			tcs.add(this[idx++])
		} else {
			tcs.add(null)
		}
		// TDi
		if (numInterface[3]) {
			tds.add(this[idx++])
		} else {
			tds.add(null)
		}

		val lastTd = tds.last()
		numInterface = lastTd?.let { bitSetOf((it.toInt() shr 4).toUByte()) }
	}

	try {
		// there are ATRs which are broken, so check if there are historical bytes to read
		val historicalBytesRaw = this.sliceArray(idx until idx + numHistorical)
		val historicalBytes = historicalBytesRaw.toHistoricalBytes()
		idx += numHistorical

		// present when at least one td encodes T > 0
		val tck =
			if (tds.find { ((it?.toInt() ?: 0) and 0xF) > 0 } != null) {
				this[idx]
			} else {
				null
			}

		return Atr(this, t0, ts, tas, tbs, tcs, tds, historicalBytes, tck)
	} catch (ex: Exception) {
		val atrStr = this.toPrintable().toString()
		log.warn(ex) { "The ATR ($atrStr) failed to parse correctly, omitting historical bytes and TCK." }
		return Atr(this, t0, ts, tas, tbs, tcs, tds, null, null)
	}
}
