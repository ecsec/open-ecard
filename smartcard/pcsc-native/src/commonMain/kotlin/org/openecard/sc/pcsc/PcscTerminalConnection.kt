package org.openecard.sc.pcsc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.feature.Feature

private val log = KotlinLogging.logger { }

class PcscTerminalConnection(
	private val hwCard: au.id.micolous.kotlin.pcsc.Card,
	override val terminal: PcscTerminal,
) : TerminalConnection {
	override fun isCardConnected(): Boolean {
		log.trace { "calling PCSC [$this] Card.status()" }
		return mapScioError { hwCard.status().present }
	}

	private var _card: PcscCard? = getCardInstance()

	private fun getCardInstance(): PcscCard? =
		mapScioError {
			log.trace { "calling PCSC [$this] Card.status()" }
			if (hwCard.status().present) PcscCard(hwCard, this) else null
		}

	override val card: PcscCard? get() {
		return _card
	}

	override fun disconnect(disposition: CardDisposition) =
		mapScioError {
			log.trace { "calling PCSC [$this] Card.disconnect($disposition)" }
			hwCard.disconnect(disposition.toPcscDisconnect())
		}

	override fun reconnect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
		disposition: CardDisposition,
	) = mapScioError {
		log.trace { "calling PCSC [$this] Card.reconnect($shareMode, $protocol, $disposition)" }
		hwCard.reconnect(shareMode.toPcsc(), setOf(protocol.toPcsc()), disposition.toPcscConnect())
		_card = getCardInstance()
	}

	fun controlCommand(
		code: Int,
		command: ByteArray,
	): ByteArray =
		mapScioError {
			val recBufSize = 8192
			log.trace { "calling PCSC [$this] Card.control($code, command=..., $recBufSize)" }
			hwCard.control(code.toLong(), command, recBufSize)!!
		}

	private val featureSet by lazy {
		val info = FeatureInfo(this)
		info.featureMap.toFeatures(this)
	}

	override fun getFeatures(): Set<Feature> = featureSet

	override fun beginTransaction() =
		mapScioError {
			log.trace { "calling PCSC [$this] Card.beginTransaction()" }
			hwCard.beginTransaction()
		}

	override fun endTransaction() =
		mapScioError {
			log.trace { "calling PCSC [$this] Card.endTransaction()" }
			hwCard.endTransaction()
		}

	override fun toString(): String = "PcscTerminalConnection(terminal=$terminal, card=$hwCard)"
}
