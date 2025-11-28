package org.openecard.sc.pace

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.SecureMessagingIndication
import org.openecard.sc.apdu.sm.SecureMessagingImpl
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.CommError
import org.openecard.sc.iface.InsufficientBuffer
import org.openecard.sc.iface.InternalSystemError
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.InvalidParameter
import org.openecard.sc.iface.InvalidValue
import org.openecard.sc.iface.NoService
import org.openecard.sc.iface.NotTransacted
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.RemovedCard
import org.openecard.sc.iface.ResetCard
import org.openecard.sc.iface.SmartcardException
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
import kotlin.coroutines.cancellation.CancellationException

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
			try {
				val paceInfos =
					efca.paceInfo.first {
						it.info.isStandardizedParameter &&
							it.supports(SUPPORTED_PACE_PROTOCOLS, SUPPORTED_PACE_DOMAIN_PARAMS)
					}

				val paceProcess =
					PaceProcess(paceInfos, channel, req.pinId, requireNotNull(req.pin), req.chat?.v, req.certDesc?.v)
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
						enforceExtLenSmApdu = true,
					)
				// remove old and set secure messaging in channel
				channel.removeSecureMessaging()
				channel.setSecureMessaging(sm)

				// the PACE establish channel response expects a key without encoding byte, so cut it off here
				val idIccRaw = paceResult.idIcc?.let { idIcc -> idIcc.sliceArray(1 until idIcc.size) }

				return PaceEstablishChannelResponse(
					paceResult.mseStatus,
					efca.efCaData.toPrintable(),
					paceResult.currentCar?.toPrintable(),
					paceResult.previousCar?.toPrintable(),
					idIccRaw?.toPrintable(),
				)
			} catch (ex: Exception) {
				when (ex) {
					is InsufficientBuffer,
					is InvalidHandle,
					is InvalidParameter,
					is InvalidValue,
					is NoService,
					is NotTransacted,
					is ReaderUnavailable,
					is CommError,
					is ResetCard,
					is RemovedCard,
					is PaceError,
					is InternalSystemError,
					is CancellationException,
					-> throw ex

					else -> throw InternalSystemError(msg = "Unexpected error: ${ex.message}", cause = ex)
				}
			}
		}

		override fun getPaceCapabilities(): Set<PaceCapability> =
			setOf(PaceCapability.GENERIC_PACE, PaceCapability.GERMAN_EID, PaceCapability.QES, PaceCapability.DESTROY_CHANNEL)

		override fun destroyChannel() {
			channel.removeSecureMessaging()
		}
	}
