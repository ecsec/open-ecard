package org.openecard.sc.iface

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.command.EnvelopeApdu
import org.openecard.sc.apdu.command.GetResponse
import org.openecard.utils.common.mergeToArray

private val log = KotlinLogging.logger { }

abstract class AbstractCardChannel : CardChannel {
	override var capabilities: CardCapabilities? = null

	private val smHandler: MutableList<SecureMessaging> = mutableListOf()

	override fun pushSecureMessaging(sm: SecureMessaging) {
		smHandler.add(sm)
	}

	override fun popSecureMessaging() {
		smHandler.removeLastOrNull()
	}

	override fun cleanSecureMessaging() {
		smHandler.clear()
	}

	protected abstract fun transmitRaw(apdu: CommandApdu): ResponseApdu

	override fun transmit(apdu: CommandApdu): ResponseApdu {
		val command = smHandler.foldRight(apdu) { sm, apdu -> sm.processRequest(apdu) }

		val responseSm =
			if (doChaining(command)) {
				val chainedApdus = toEnvelopeApdus(command)
				executeChainedApdus(chainedApdus)
			} else {
				// just send as is and hope for the best
				val res = transmitRaw(command)

				if (res.status.type == StatusWord.WRONG_LE) {
					// - If SW1 is set to '6C', then the process is aborted and before issuing any other command, the same command may
					//   be re-issued using SW2 (exact number of available data bytes) as short Le field
					log.debug { "Wrong length status returned, reissuing command with adjusted length field." }
					val newCommand = command.copy(le = res.status.sw2.toUShort(), forceExtendedLength = false)
					transmitRaw(newCommand)
				} else {
					res
				}
			}

		// check if the result indicates the need for GET RESPONSE
		val responseSmAggregated = handleGetResponse(responseSm)

		val response = smHandler.fold(responseSmAggregated) { res, sm -> sm.processResponse(res) }
		return response
	}

	protected fun doChaining(apdu: CommandApdu): Boolean {
		val hasExtLen = capabilities?.commandCoding?.supportsExtendedLength ?: false
		val hasChaining = capabilities?.commandCoding?.supportsCommandChaining ?: false
		return apdu.lc > 0xFFu && !hasExtLen && hasChaining
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	protected fun toEnvelopeApdus(apdu: CommandApdu): List<CommandApdu> {
		val chunks = apdu.toBytes.chunked(0xFF) { it.toUByteArray() }
		return chunks.mapIndexed { i, next ->
			val isLast = i == chunks.size - 1
			// TODO: find out if we need to provide le
			EnvelopeApdu(!isLast, next, null).apdu
		}
	}

	protected fun executeChainedApdus(apdus: List<CommandApdu>): ResponseApdu {
		return apdus
			.map { apdu ->
				val res = transmitRaw(apdu)
				if (res.status.type != StatusWord.OK) {
					return res
				} else {
					res
				}
			}.last()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	protected fun handleGetResponse(initialData: ResponseApdu): ResponseApdu {
		if (initialData.status.type != StatusWord.MORE_BYTES_AVAILABLE) {
			return initialData
		}

		var nextCmd = GetResponse(initialData.sw2.toUShort()).apdu

		val result = mutableListOf<UByteArray>()
		result.add(initialData.data)

		do {
			val response = transmitRaw(nextCmd)
			result.add(response.data)

			if (response.status.type == StatusWord.MORE_BYTES_AVAILABLE) {
				// again with missing bytes
				nextCmd = GetResponse(response.sw2.toUShort()).apdu
			} else {
				// no more bytes available, note that sw could also mean error
				val data = result.mergeToArray()
				return ResponseApdu(data, response.sw1, response.sw2)
			}
		} while (true)
	}
}
