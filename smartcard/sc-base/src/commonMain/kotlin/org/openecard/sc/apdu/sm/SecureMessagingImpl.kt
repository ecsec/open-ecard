package org.openecard.sc.apdu.sm

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.SecureMessagingIndication
import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.isNormalProcessed
import org.openecard.sc.apdu.matchStatus
import org.openecard.sc.iface.InvalidApduStatus
import org.openecard.sc.iface.InvalidSmDo
import org.openecard.sc.iface.InvalidSwData
import org.openecard.sc.iface.MissingSmDo
import org.openecard.sc.iface.NoSwData
import org.openecard.sc.iface.SecureMessaging
import org.openecard.sc.iface.SecureMessagingUnsupported
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.sc.tlv.toTlvBer
import org.openecard.utils.common.mergeToArray
import org.openecard.utils.common.toUByteArray
import org.openecard.utils.serialization.toPrintable

class SecureMessagingImpl(
	val commandStages: List<CommandStage>,
	val responseStages: List<ResponseStage>,
	val smType: SecureMessagingIndication,
	val protectedData: Boolean,
	val protectedLe: Boolean,
	val protectedHeader: Boolean,
) : SecureMessaging {
	init {
		require(commandStages.isNotEmpty())
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processRequest(requestApdu: CommandApdu): CommandApdu {
		// prepare DOs
		val initialDos: List<Tlv> = commandToTlv(requestApdu)

		// update header
		val origCla = requireNotNull(requestApdu.classByteInterIndustry) { "Command APDU uses a proprietary class byte" }
		val newCla = origCla.setSecureMessaging(smType)
		val smRequestTemplate = requestApdu.copy(cla = newCla.byte, le = 0u)

		// calculate data field
		val protectedDos =
			commandStages.fold(
				initialDos,
			) { last, stage -> stage.processCommand(smRequestTemplate, last) }
		val protectedData = protectedDos.map { it.toBer() }.mergeToArray()

		// update data field
		val smRequest = smRequestTemplate.copy(data = protectedData.toPrintable())
		return smRequest
	}

	/**
	 * Convert the command APDU to a set of DOs usable in stages.
	 */
	@OptIn(ExperimentalUnsignedTypes::class)
	private fun commandToTlv(requestApdu: CommandApdu): List<Tlv> =
		buildList {
			// protected header DO only if not protected by checksum directly
			if (smType != SecureMessagingIndication.SM_W_HEADER && protectedHeader) {
				val header = ubyteArrayOf(requestApdu.cla, requestApdu.ins, requestApdu.p1, requestApdu.p2)
				add(TlvPrimitive(SmBasicTags.commandHeader.tag, header.toPrintable()))
			}
			// command data
			if (requestApdu.data.v.isNotEmpty()) {
				val tag =
					if (protectedData) {
						SmBasicTags.plain.auth.tag
					} else {
						SmBasicTags.plain.unauth.tag
					}
				add(TlvPrimitive(tag, requestApdu.data))
			}
			// le
			requestApdu.le?.let { le ->
				val le =
					if (requestApdu.forceExtendedLength || le > 0xFFu) {
						le.toUByteArray()
					} else {
						le.toUByte().toUByteArray()
					}
				val tag =
					if (protectedLe) {
						SmBasicTags.expectedLength.auth.tag
					} else {
						SmBasicTags.expectedLength.unauth.tag
					}
				add(TlvPrimitive(tag, le.toPrintable()))
			}
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun processResponse(responseApdu: ResponseApdu): ResponseApdu {
		if (responseApdu.matchStatus(StatusWord.SM_DO_MISSING)) throw MissingSmDo()
		if (responseApdu.matchStatus(StatusWord.SM_DO_INCORRECT)) throw InvalidSmDo()
		if (responseApdu.matchStatus(StatusWord.SECURE_MESSAGING_UNSUPPORTED)) throw SecureMessagingUnsupported()
		// sm response must be 9000
		if (!responseApdu.isNormalProcessed) throw InvalidApduStatus()

		val protectedDos =
			responseApdu.data
				.toTlvBer()
				.tlv
				.asList()
		val unprotectedDos = responseStages.fold(protectedDos) { last, stage -> stage.processResponse(last) }

		// reconstruct response APDU
		val data = unprotectedDos.find { it.tag in SmBasicTags.plain.tags }?.contentAsBytesBer ?: ubyteArrayOf()
		val swData =
			unprotectedDos.find { it.tag == SmBasicTags.sw.tag }?.contentAsBytesBer ?: throw NoSwData()
		if (swData.size != 2) {
			throw InvalidSwData()
		}

		return ResponseApdu(data, swData[1], swData[0])
	}
}
