package org.openecard.sc.iface.info

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.apdu.command.ReadBinary
import org.openecard.sc.apdu.command.ReadRecord
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.Atr
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.tlv.toTlvBer

private val log = KotlinLogging.logger { }

class SmartcardInfoRetriever(
	val channel: CardChannel,
) {
	val atr: Atr = channel.card.atr
	var efAtr: EfAtr? = null
	var efDir: EfDir? = null

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun readEfAtrTransparent() {
		if (efAtr == null) {
			try {
				Select.selectEfIdentifier(0x2F01u).transmit(channel)
				val efAtrData = ReadBinary.readCurrentEf().transmit(channel)
				val efAtrTlv = efAtrData.toTlvBer()
				this.efAtr = EfAtr(efAtrTlv.tlv.asList())
			} catch (ex: Exception) {
				log.info(ex) { "Failed to read EF.ATR as transparent file" }
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun readEfAtrRecord() {
		if (efAtr == null) {
			try {
				Select.selectEfIdentifier(0x2F01u).transmit(channel)
				val efAtrData = ReadRecord.readAllRecords().transmit(channel)
				val efAtrTlv = efAtrData.toTlvBer()
				this.efAtr = EfAtr(efAtrTlv.tlv.asList())
			} catch (ex: Exception) {
				log.info(ex) { "Failed to read EF.ATR as record file" }
			}
		}
	}

	private fun readEfAtrGetData() {
		if (efAtr == null) {
			// 7.4.2 GET DATA command
			// TODO: Implement
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun readEfDirTransparent() {
		if (efDir == null) {
			try {
				Select.selectEfIdentifier(0x2F00u).transmit(channel)
				val efDirData = ReadBinary.readCurrentEf().transmit(channel)
				val efDirTlv = efDirData.toTlvBer()
				this.efDir = EfDir(efDirTlv.tlv.asList())
			} catch (ex: Exception) {
				log.info(ex) { "Failed to read EF.DIR as transparent file" }
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun readEfDirRecord() {
		if (efDir == null) {
			try {
				Select.selectEfIdentifier(0x2F00u).transmit(channel)
				val efDirData = ReadRecord.readAllRecords().transmit(channel)
				val efDirTlv = efDirData.toTlvBer()
				this.efDir = EfDir(efDirTlv.tlv.asList())
			} catch (ex: Exception) {
				log.info(ex) { "Failed to read EF.DIR as record file" }
			}
		}
	}

	private fun readEfDirGetData() {
		if (efDir == null) {
			// 7.4.2 GET DATA command
			// TODO: Implement
		}
	}

	fun retrieve(
		withSelectMf: Boolean = true,
		withEfAtr: Boolean = true,
		withEfDir: Boolean = true,
	): SmartcardInfo {
		val hb = atr.historicalBytes

		if (withSelectMf) {
			runCatching { Select.selectMf().transmit(channel) }
		}

		if (withEfAtr) {
			// read ef.atr based on historical bytes
			hb?.cardServiceData?.also { csd ->
				if (csd.hasEfAtr && csd.efDirAtrReadBinary) {
					readEfAtrTransparent()
				} else if (csd.hasEfAtr && csd.efDirAtrReadRecord) {
					readEfAtrRecord()
				} else if (csd.hasEfAtr && csd.efDirAtrGetData) {
					readEfAtrGetData()
				}
			} ?: run {
				// try all methods
				readEfAtrTransparent()
				readEfAtrRecord()
				readEfAtrGetData()
			}
		}

		if (withEfDir) {
			// read ef.dir based on historical bytes
			(hb?.cardServiceData ?: efAtr?.historicalBytes?.cardServiceData)?.also { csd ->
				if (csd.hasEfDir && csd.efDirAtrReadBinary) {
					readEfDirTransparent()
				} else if (csd.hasEfDir && csd.efDirAtrReadRecord) {
					readEfDirRecord()
				} else if (csd.hasEfDir && csd.efDirAtrGetData) {
					readEfDirGetData()
				}
			} ?: run {
				// try all methods
				readEfDirTransparent()
				readEfDirRecord()
				readEfDirGetData()
			}
		}

		return SmartcardInfo(atr, efAtr, efDir)
	}
}
