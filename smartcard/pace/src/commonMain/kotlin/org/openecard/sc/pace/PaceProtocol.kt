package org.openecard.sc.pace

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.SecureMessagingIndication
import org.openecard.sc.apdu.sm.SecureMessagingImpl
import org.openecard.sc.iface.CardChannel
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
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.iface.feature.PaceResultCode
import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.oid.PaceObjectIdentifier
import org.openecard.utils.serialization.toPrintable

class PaceProtocol {
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
	)
	fun execute(
		channel: CardChannel,
		pinId: PacePinId,
		pin: String,
		/**
		 * The following elements are only present if the execution of PACE is to be followed by an
		 * execution of Terminal Authentication Version 2 as defined in [TR-03110].
		 */
		chat: UByteArray?,
	): PaceEstablishChannelResponse {
		val efca =
			runCatching { EfCardAccess.readEfCardAccess(channel) }
				.onFailure {
					if (it is ApduProcessingError) {
						throw PaceError(PaceResultCode.READ_EFCA_ERROR, it.status)
					}
				}.getOrThrow()
		val paceInfos =
			efca.paceInfo.first {
				it.info.isStandardizedParameter &&
					it.supports(SUPPORTED_PACE_PROTOCOLS, SUPPORTED_PACE_DOMAIN_PARAMS)
			}

		val paceProcess = PaceProcess(paceInfos, channel, pinId, pin, chat)
		val paceResult = paceProcess.execute()

		val encKey = paceResult.encKey
		val macKey = paceResult.macKey

		// prepare secure messaging object
		val encStage = EncryptionStage(encKey.toByteArray())
		val macStage = CmacStage(macKey.toByteArray())
		val sm =
			SecureMessagingImpl(
				commandStages = listOf(encStage, macStage),
				responseStages = listOf(macStage, encStage),
				smType = SecureMessagingIndication.SM_W_HEADER,
				protectedData = true,
				protectedLe = true,
				protectedHeader = false,
			)
		// set secure messaging in channel
		channel.setSecureMessaging(sm)

		return PaceEstablishChannelResponse(
			paceResult.mseStatus,
			efca.efCaData.toPrintable(),
			paceResult.currentCar?.toPrintable(),
			paceResult.previousCar?.toPrintable(),
			paceResult.idIcc?.toPrintable(),
		)
	}
}

private val SUPPORTED_PACE_PROTOCOLS =
	setOf(
		PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128,
		PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_192,
		PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_256,
	)

private val SUPPORTED_PACE_DOMAIN_PARAMS =
	setOf(
		10u, // NIST P-224 (secp224r1)
		11u, // BrainpoolP224r1
		12u, // NIST P-256 (secp256r1)
		13u, // BrainpoolP256r1
		14u, // BrainpoolP320r1
		15u, // NIST P-384 (secp384r1)
		16u, // BrainpoolP384r1
		17u, // BrainpoolP512r1
		18u, // NIST P-521 (secp521r1)
	)
