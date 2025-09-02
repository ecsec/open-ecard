package org.openecard.addons.tr03124.eac

import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.InvalidServerData
import org.openecard.addons.tr03124.UserCanceled
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.addons.tr03124.xml.DidAuthenticateRequest
import org.openecard.addons.tr03124.xml.Eac1Input
import org.openecard.addons.tr03124.xml.Eac1Output
import org.openecard.addons.tr03124.xml.Eac2Input
import org.openecard.addons.tr03124.xml.EacAdditionalInput
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.acl.PaceAclQualifier
import org.openecard.sal.iface.DeviceConnection
import org.openecard.sal.iface.DeviceUnsupported
import org.openecard.sal.sc.SmartcardDeviceConnection
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sal.sc.dids.SmartcardPaceDid
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.pace.asn1.EfCardAccess.Companion.toEfCardAccess
import org.openecard.sc.pace.cvc.AuthenticationTerminalChat
import org.openecard.sc.pace.cvc.CardVerifiableCertificate
import org.openecard.sc.pace.cvc.CardVerifiableCertificate.Companion.toCardVerifiableCertificate
import org.openecard.sc.pace.cvc.CertificateDescription
import org.openecard.sc.pace.cvc.CertificateDescription.Companion.toCertificateDescription
import org.openecard.sc.pace.cvc.PublicKeyReference
import org.openecard.sc.pace.cvc.PublicKeyReference.Companion.toPublicKeyReference
import org.openecard.sc.tlv.toTlvBer
import org.openecard.utils.common.cast
import org.openecard.utils.serialization.toPrintable
import java.lang.IllegalStateException

internal class UiStepImpl(
	private val ctx: UiStepCtx,
) : UiStep {
	internal class UiStepCtx(
		val session: SmartcardSalSession,
		var card: SmartcardDeviceConnection?,
		val token: TcToken,
		val eserviceClient: EserviceClient,
		val eidServer: EidServerInterface,
		val eac1InputReq: DidAuthenticateRequest,
		val cvcs: List<CardVerifiableCertificate>,
		val certDesc: CertificateDescription,
		val requiredChat: AuthenticationTerminalChat,
		val optionalChat: AuthenticationTerminalChat,
		private var _terminalName: String?,
	) {
		var terminalName: String
			get() = _terminalName ?: throw IllegalStateException("No terminal name has been specified for the EAC process")
			set(value) {
				if (value != _terminalName) {
					_terminalName = value
					disconnectCard()
				}
			}

		fun disconnectCard() {
			card?.let {
				runCatching { it.close(CardDisposition.LEAVE) }
				card = null
			}
		}
	}

	override val guiData: EacUiData = EacUiData(ctx.certDesc, ctx.requiredChat.copy(), ctx.optionalChat.copy())

	override suspend fun cancel(): BindingResponse {
		disconnectCard()
		ctx.eidServer.cancel()
		return UserCanceled(ctx.eserviceClient).toResponse()
	}

	override fun getPaceDid(terminalName: String?): SmartcardPaceDid {
		// update terminal name and connection if needed
		terminalName?.let { ctx.terminalName = terminalName }
		val con = connectIfNeeded()
		val app = checkNotNull(con.applications.find { it.name == NpaDefinitions.Apps.Mf.name })
		val pace =
			checkNotNull(
				app.dids.filterIsInstance<SmartcardPaceDid>().find {
					it.name == getPinDidName()
				},
			) { "Required PACE DID is not available" }
		return pace
	}

	private fun getPinDidName(): String {
		val didName: String = TODO() // ctx.eac1InputReq.didName
		when (didName) {
			"PIN" -> NpaDefinitions.Apps.Mf.Dids.pacePin
			"CAN" -> NpaDefinitions.Apps.Mf.Dids.paceCan
			"PUK" -> NpaDefinitions.Apps.Mf.Dids.pacePuk
			else -> throw IllegalArgumentException("Unknown DID name received from eID-Server")
		}
	}

	@Throws(DeviceUnsupported::class)
	private fun connectIfNeeded(): DeviceConnection =
		ctx.card ?: run {
			val card = ctx.session.connect(ctx.terminalName)
			if (card.deviceType != NpaDefinitions.cardType) {
				runCatching { card.close(CardDisposition.LEAVE) }
				throw DeviceUnsupported("Connected card is of type ${card.deviceType}, which is unsupported")
			}
			ctx.card = card
			card
		}

	override fun disconnectCard() {
		ctx.disconnectCard()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun processAuthentication(paceResponse: PaceEstablishChannelResponse): EidServerStep {
		val pace = getPaceDid()
		check(pace.missingAuthAuthentications.isSolved)

		val efCa = paceResponse.efCardAccess.v.toEfCardAccess()
		val ta = TerminalAuthenticationImpl(pace.application.device, efCa)
		val ca = ChipAuthenticationImpl(pace.application.device, pace)

		val challenge = ta.challenge
		val chat =
			checkNotNull(
				pace
					.toStateReference()
					.stateQualifier
					?.cast<PaceAclQualifier>()
					?.chat,
			) {
				"PACE DID does not contain CHAT used for authentication"
			}
		val cars =
			listOfNotNull(paceResponse.carCurr, paceResponse.carPrev).map {
				it.v
					.toPublicKeyReference()
					.joinToString()
			}
		val idPicc = checkNotNull(paceResponse.idIcc) { "PACE did not yield a ID_PICC value, which is required for EAC" }
		val eac1Out =
			Eac1Output(
				protocol = ctx.eac1InputReq.data.protocol,
				certificateHolderAuthorizationTemplate = chat,
				certificationAuthorityReference = cars,
				efCardAccess = paceResponse.efCardAccess,
				idPICC = idPicc,
				challenge = challenge.toPrintable(),
			)
		val eac2In =
			when (val msg = ctx.eidServer.sendDidAuthResponse(eac1Out)) {
				is Eac2Input -> msg
				else -> throw InvalidServerData(ctx.eserviceClient, "")
			}

		val eacAuth: EacAuthentication = EacAuthenticationImpl(ta, ca, eac2In)

		val outMsg = eacAuth.process()
		ctx.eidServer.sendDidAuthResponse(outMsg)?.let {
			val additionalMsg =
				it.cast<EacAdditionalInput>()
					?: throw InvalidServerData(ctx.eserviceClient, "Expecting an EacAdditionalInput message")
			val caOutMsg = eacAuth.processAdditional(additionalMsg)
			// send and fail if don't get a command message (e.g. Transmit)
			if (ctx.eidServer.sendDidAuthResponse(caOutMsg) != null) {
				throw InvalidServerData(ctx.eserviceClient, "Expecting a data command message")
			}
		}

		return EidServerStepImpl()
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		internal fun createStep(
			session: SmartcardSalSession,
			terminalName: String?,
			token: TcToken,
			eserviceClient: EserviceClient,
			eidServer: EidServerInterface,
			eac1InputReq: DidAuthenticateRequest,
		): UiStep {
			val certsRaw: List<UByteArray> = TODO() // eac1InputReq.data.certificates
			val certs = certsRaw.map { it.toCardVerifiableCertificate() }
			val certDescRaw: UByteArray = TODO() // eac1InputReq.data.certificateDescription
			val certDesc = certDescRaw.toCertificateDescription()

			// update allowed certificates in eService connection, failing when we already see a problem
			eserviceClient.certTracker.setCertDesc(certDesc)

			// find chats
			val optChat =
				TODO()
					// eac1InputReq.data.optionalChat
					?: run {
						// use CHAT from certificate as upper bound, if there is no optional chat specified
						val cert =
							requireNotNull(certs.find { it.isTerminalCertificate }) { "No terminal certificate in received certificates" }
						requireNotNull(
							cert.chat.cast<AuthenticationTerminalChat>(),
						) { "CHAT in terminal certificate is of the wrong type" }
					}
			// use optional chat as lower bound, when there is nothing specified
			val reqChat =
				TODO()
					// eac1InputReq.data.requiredChat
					?: optChat

			val ctx =
				UiStepCtx(
					session,
					null,
					token,
					eserviceClient,
					eidServer,
					eac1InputReq,
					certs,
					certDesc,
					reqChat,
					optChat,
					terminalName,
				)
			return UiStepImpl(ctx)
		}
	}
}
