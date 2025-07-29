package org.openecard.sal.sc.dids

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.did.PaceDidDefinition
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.PasswordError
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.mapSmartcardError
import org.openecard.sal.sc.mapSmartcardErrorSuspending
import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.command.SecurityCommandResult
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.feature
import org.openecard.sc.iface.feature.PaceCapability
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PaceEstablishChannelRequest
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.feature.PaceFeatureFactory
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.iface.feature.PaceResultCode
import org.openecard.sc.pace.apdu.paceMseSetAt
import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.asn1.EfCardAccess.Companion.SUPPORTED_PACE_DOMAIN_PARAMS
import org.openecard.sc.pace.asn1.EfCardAccess.Companion.SUPPORTED_PACE_PROTOCOLS
import org.openecard.utils.common.throwIf
import org.openecard.utils.serialization.toPrintable

private val log = KotlinLogging.logger {}

class SmartcardPaceDid(
	application: SmartcardApplication,
	didDef: PaceDidDefinition,
	val authAcl: CifAclOr,
	private val factory: PaceFeatureFactory,
) : SmartcardDid.BaseSmartcardDid<PaceDidDefinition>(didDef, application),
	PaceDid {
	override val pinType: PacePinId = did.parameters.passwordRef.toSalType()

	override val missingAuthAuthentications: MissingAuthentications
		get() = missingAuthentications(authAcl)

	private val channel: CardChannel
		get() = application.channel

	private val hardwarePace: PaceFeature? by lazy {
		channel.card.terminalConnection.feature<PaceFeature>()
	}

	private val efCardAccess by lazy {
		runCatching { EfCardAccess.readEfCardAccess(channel) }
			.onFailure {
				if (it is ApduProcessingError) {
					throw PaceError(PaceResultCode.READ_EFCA_ERROR, it.status)
				}
			}.getOrThrow()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private val paceFeature by lazy {
		hardwarePace ?: factory.create(channel, efCardAccess.efCaData)
	}

	// TODO: implement and add state qualifier
	override fun toStateReference(): DidStateReference = super.toStateReference()

	override fun capturePasswordInHardware(): Boolean = mapSmartcardError { hardwarePace != null }

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun passwordStatus(): SecurityCommandResult =
		mapSmartcardError {
			val paceInfos =
				efCardAccess.paceInfo.first {
					it.info.isStandardizedParameter &&
						it.supports(SUPPORTED_PACE_PROTOCOLS, SUPPORTED_PACE_DOMAIN_PARAMS)
				}
			val command = paceMseSetAt(paceInfos, pinType, null, null)
			val commandResult = command.transmit(channel)
			commandResult
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun establishChannel(
		password: String,
		chat: UByteArray?,
		certDesc: UByteArray?,
	): PaceEstablishChannelResponse =
		mapSmartcardError {
			throwIf(!missingAuthAuthentications.isSolved) { MissingAuthentication("Authenticate ACL is not satisfied") }

			val req = PaceEstablishChannelRequest(pinType, checkPassword(password), chat?.toPrintable(), certDesc?.toPrintable())
			runBlocking(Dispatchers.IO) {
				val resp = paceFeature.establishChannel(req)
				unsetAuthOfOtherPaceDids()
				setDidFulfilled()
				resp
			}
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun establishChannel(
		chat: UByteArray?,
		certDesc: UByteArray?,
	): PaceEstablishChannelResponse =
		mapSmartcardErrorSuspending {
			throwIf(!missingAuthAuthentications.isSolved) { MissingAuthentication("Authenticate ACL is not satisfied") }

			val req = PaceEstablishChannelRequest(pinType, null, chat?.toPrintable(), certDesc?.toPrintable())
			val resp = paceFeature.establishChannel(req)
			unsetAuthOfOtherPaceDids()
			setDidFulfilled()
			resp
		}

	private fun checkPassword(pass: String): String {
		val minLen = did.parameters.minLength
		val maxLen = did.parameters.maxLength
		throwIf(pass.length < minLen) { PasswordError(PasswordError.PasswordErrorType.TOO_SHORT) }
		throwIf(maxLen != null && pass.length > maxLen) { PasswordError(PasswordError.PasswordErrorType.TOO_LONG) }

		return pass
	}

	override fun closeChannel() =
		mapSmartcardError {
			if (paceFeature.canCloseChannel()) {
				paceFeature.destroyChannel()
			}
			setDidUnfulfilled()
		}

	private fun unsetAuthOfOtherPaceDids() {
		application.device.authenticatedDids.filterIsInstance<SmartcardPaceDid>().forEach { did ->
			did.setDidUnfulfilled()
		}
	}
}

internal fun org.openecard.cif.definition.did.PacePinId.toSalType() =
	when (this) {
		org.openecard.cif.definition.did.PacePinId.MRZ -> PacePinId.MRZ
		org.openecard.cif.definition.did.PacePinId.CAN -> PacePinId.CAN
		org.openecard.cif.definition.did.PacePinId.PIN -> PacePinId.PIN
		org.openecard.cif.definition.did.PacePinId.PUK -> PacePinId.PUK
	}

private fun PaceFeature.canCloseChannel(): Boolean = PaceCapability.DESTROY_CHANNEL in getPaceCapabilities()
