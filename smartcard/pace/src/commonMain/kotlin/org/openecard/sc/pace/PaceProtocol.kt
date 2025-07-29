package org.openecard.sc.pace

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.SecureMessagingIndication
import org.openecard.sc.apdu.sm.SecureMessagingImpl
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.feature.PaceCapability
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PaceEstablishChannelRequest
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.feature.PaceResultCode
import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.asn1.EfCardAccess.Companion.SUPPORTED_PACE_DOMAIN_PARAMS
import org.openecard.sc.pace.asn1.EfCardAccess.Companion.SUPPORTED_PACE_PROTOCOLS
import org.openecard.sc.pace.asn1.EfCardAccess.Companion.toEfCardAccess
import org.openecard.utils.serialization.toPrintable

class PaceProtocol
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val channel: CardChannel,
		/**
		 * Externally provided EF.CardAccess.
		 * If it is not provided, establishChannel reads it from the card.
		 */
		externalEfCardAccess: UByteArray? = null,
	) : PaceFeature {
		@OptIn(ExperimentalUnsignedTypes::class)
		private val efca by lazy {
			externalEfCardAccess?.toEfCardAccess()
				?: runCatching { EfCardAccess.readEfCardAccess(channel) }
					.onFailure {
						if (it is ApduProcessingError) {
							throw PaceError(PaceResultCode.READ_EFCA_ERROR, it.status)
						}
					}.getOrThrow()
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		override suspend fun establishChannel(req: PaceEstablishChannelRequest): PaceEstablishChannelResponse {
			val paceInfos =
				efca.paceInfo.first {
					it.info.isStandardizedParameter &&
						it.supports(SUPPORTED_PACE_PROTOCOLS, SUPPORTED_PACE_DOMAIN_PARAMS)
				}

			val paceProcess = PaceProcess(paceInfos, channel, req.pinId, requireNotNull(req.pin), req.chat?.v, req.certDesc?.v)
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
					requireSwDo = true,
				)
			// remove old and set secure messaging in channel
			channel.removeSecureMessaging()
			channel.setSecureMessaging(sm)

			return PaceEstablishChannelResponse(
				paceResult.mseStatus,
				efca.efCaData.toPrintable(),
				paceResult.currentCar?.toPrintable(),
				paceResult.previousCar?.toPrintable(),
				paceResult.idIcc?.toPrintable(),
			)
		}

		override fun getPaceCapabilities(): Set<PaceCapability> =
			setOf(PaceCapability.GENERIC_PACE, PaceCapability.GERMAN_EID, PaceCapability.QES, PaceCapability.DESTROY_CHANNEL)

		override fun destroyChannel() {
			channel.removeSecureMessaging()
		}
	}
