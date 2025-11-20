package org.openecard.addons.tr03124.eac

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.InvalidServerData
import org.openecard.addons.tr03124.UnkownCvcChainError
import org.openecard.addons.tr03124.UserCanceled
import org.openecard.addons.tr03124.runEacCatching
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.addons.tr03124.transport.UntrustedCertificateError
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
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardDeviceConnection
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sal.sc.dids.SmartcardPaceDid
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.pace.asn1.EfCardAccess.Companion.toEfCardAccess
import org.openecard.sc.pace.cvc.AuthenticatedAuxiliaryData
import org.openecard.sc.pace.cvc.AuthenticatedAuxiliaryData.Companion.toAuthenticatedAuxiliariyData
import org.openecard.sc.pace.cvc.AuthenticationTerminalChat
import org.openecard.sc.pace.cvc.CardVerifiableCertificate
import org.openecard.sc.pace.cvc.CardVerifiableCertificate.Companion.toCardVerifiableCertificate
import org.openecard.sc.pace.cvc.CertificateDescription
import org.openecard.sc.pace.cvc.CertificateDescription.Companion.toCertificateDescription
import org.openecard.sc.pace.cvc.Chat.Companion.toChat
import org.openecard.sc.pace.cvc.CvcChain.Companion.toChain
import org.openecard.sc.pace.cvc.PublicKeyReference.Companion.toPublicKeyReference
import org.openecard.sc.tlv.TlvException
import org.openecard.sc.tlv.toTlvBer
import org.openecard.utils.common.cast
import org.openecard.utils.common.throwIf
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

private val log = KotlinLogging.logger { }

internal class UiStepImpl(
	private val ctx: UiStepCtx,
) : UiStep {
	internal class UiStepCtx(
		val session: SmartcardSalSession,
		var card: SmartcardDeviceConnection?,
		var pace: PaceDid?,
		val token: TcToken,
		val eserviceClient: EserviceClient,
		val eidServer: EidServerInterface,
		val eac1InputReq: DidAuthenticateRequest,
		val eac1Input: Eac1Input,
		val cvcs: List<CardVerifiableCertificate>,
		val terminalCert: CardVerifiableCertificate,
		val certDesc: CertificateDescription,
		val terminalCertChat: AuthenticationTerminalChat,
		val requiredChat: AuthenticationTerminalChat,
		val optionalChat: AuthenticationTerminalChat,
		val aad: AuthenticatedAuxiliaryData?,
		val paceDid: PacePinId,
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
			pace?.let {
				runCatching {
					log.debug { "Closing PACE channel" }
					pace?.closeChannel()
				}
				pace = null
			}
			card?.let {
				runCatching {
					log.debug { "Disconnect eID-Card" }
					it.close(CardDisposition.RESET)
				}
				card = null
			}
		}
	}

	override val guiData: EacUiData =
		EacUiData(
			ctx.terminalCert,
			ctx.certDesc,
			ctx.terminalCertChat.copy(),
			ctx.requiredChat.copy(),
			ctx.optionalChat.copy(),
			ctx.aad,
			ctx.eac1Input.transactionInfo,
			ctx.paceDid,
			ctx.eac1Input.acceptedEidType,
		)

	override suspend fun cancel(): BindingResponse {
		try {
			log.info { "EAC UI Step cancelled" }
			disconnectCard()
			ctx.eidServer.sendError(UserCanceled(ctx.eserviceClient))
		} catch (ex: BindingException) {
			return ex.toResponse()
		}
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

		// set PACE DID, so we can destroy the channel later
		ctx.pace = pace

		return pace
	}

	private fun getPinDidName(): String {
		val didName: String = ctx.eac1InputReq.didName
		return when (didName) {
			"PIN" -> NpaDefinitions.Apps.Mf.Dids.pacePin
			"CAN" -> NpaDefinitions.Apps.Mf.Dids.paceCan
			"PUK" -> NpaDefinitions.Apps.Mf.Dids.pacePuk
			else -> throw IllegalArgumentException("Unknown DID name received from eID-Server")
		}
	}

	@Throws(DeviceUnsupported::class)
	private fun connectIfNeeded(): DeviceConnection =
		ctx.card ?: run {
			val card = ctx.session.connect(ctx.terminalName, false)

			// force the card to be recognized as contactless, as there might be readers such as PersoSIM which don't
			// detect it properly
			card.channel.card.setContactless = true

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
	override suspend fun processAuthentication(paceResponse: PaceEstablishChannelResponse): EidServerStep =
		runCatching {
			runEacCatching(ctx.eserviceClient, ctx.eidServer) {
				log.info { "Processing PACE response" }
				val pace = getPaceDid()
				check(pace.missingAuthAuthentications.isSolved)

				val efCa = paceResponse.efCardAccess.v.toEfCardAccess()
				val ta = TerminalAuthenticationImpl(pace.application.device, efCa)
				val ca = ChipAuthenticationImpl(pace.application.device, pace, efCa)

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
				val idPicc =
					checkNotNull(paceResponse.idIcc?.v) { "PACE did not yield a ID_PICC value, which is required for EAC" }

				val cars =
					listOfNotNull(paceResponse.carCurr, paceResponse.carPrev).map {
						it.v.toPublicKeyReference()
					}
				val preliminaryChains = cars.mapNotNull { ctx.cvcs.toChain(it) }
				val preliminaryChain = preliminaryChains.firstOrNull()

				// send cars when we don't have a chain
				val carsParam =
					if (preliminaryChain == null) {
						log.debug { "Preliminary CVC chain could not be built" }
						cars.map { it.joinToString() }
					} else {
						log.debug { "CVC chain successfully built" }
						listOf()
					}
				val eac1Out =
					Eac1Output(
						protocol = ctx.eac1InputReq.data.protocol,
						certificateHolderAuthorizationTemplate = chat,
						certificationAuthorityReference = carsParam,
						efCardAccess = paceResponse.efCardAccess,
						idPICC = idPicc.toPrintable(),
						challenge = challenge.toPrintable(),
					)
				val eac2In =
					when (val msg = ctx.eidServer.sendDidAuthResponse(eac1Out)) {
						is Eac2Input -> msg
						else -> throw InvalidServerData(
							ctx.eserviceClient,
							"EAC2Input message expected, but server sent something else",
						)
					}

				val aad =
					ctx.eac1Input.authenticatedAuxiliaryData
						?.v
						?.toTlvBer()
						?.tlv

				val chain =
					preliminaryChain ?: run {
						log.debug { "Building CVC chain with additional certificates from the eID-Server" }
						// build chain with the data we have and the additional certificates
						val additionalCvcs =
							eac2In.certificates.map {
								runCatching {
									it.v.toCardVerifiableCertificate()
								}.getOrElse { ex -> throw InvalidServerData(ctx.eserviceClient, "Invalid CVC received in EAC2Input", ex) }
							}
						val cvcs = ctx.cvcs + additionalCvcs
						val chains =
							cars.mapNotNull { cvcs.toChain(it) }

						chains.firstOrNull()
							?: run {
								ctx.eidServer.sendError(
									UnkownCvcChainError(ctx.eserviceClient, "Unknown trust chain referenced by CAR"),
								)
							}
					}

				val eacAuth: EacAuthentication = EacAuthenticationImpl(ta, ca, eac2In, chain, aad)

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

				EidServerStepImpl(this, ctx.eserviceClient, ctx.eidServer, pace.application.device)
			}
		}.onFailure { ex ->
			log.warn(ex.takeIf { log.isDebugEnabled() }) { "Received error while processing EAC protocol: ${ex.message}" }
			disconnectCard()
		}.getOrThrow()

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
			log.info { "Creating EAC UI Step" }
			try {
				val eac1Input: Eac1Input = eac1InputReq.data as Eac1Input
				val certsRaw = eac1Input.certificates
				val certs = certsRaw.map { it.v.toCardVerifiableCertificate() }
				val certDescRaw = eac1Input.certificateDescription
				val certDesc = certDescRaw.v.toCertificateDescription()

				// update allowed certificates in eService connection, failing when we already see a problem
				eserviceClient.certTracker.setCertDesc(certDesc)
				// check that cert desc contains subjectUrl (optional in data structure, but required here)
				val subjectUrl =
					requireNotNull(certDesc.subjectUrl) {
						"Subject URL is missing in CertificateDescription"
					}
				// check that tctoken and subjectUrl have matching sop
				throwIf(
					!eserviceClient.certTracker.matchesSop(eserviceClient.tcTokenUrl, subjectUrl, useCertDesc = false),
				) {
					UntrustedCertificateError("TCToken URL does not match Subject URL")
				}

				// find chats
				val terminalCert =
					requireNotNull(
						certs.find {
							it.isTerminalCertificate
						},
					) { "No terminal certificate in received certificates" }
				terminalCert.checkDescriptionHash(certDesc)

				// mark paos channel as validated, so further messages can be exchanged
				eidServer.setValidated()

				val terminalCertChat =
					requireNotNull(
						terminalCert.chat.cast<AuthenticationTerminalChat>(),
					) { "CHAT in terminal certificate is of the wrong type" }

				val optChat =
					eac1Input.optionalChat?.toAuthenticationTerminalChat()
						?: terminalCertChat
				// use optional chat as lower bound, when there is nothing specified
				val reqChat =
					eac1Input.requiredChat?.toAuthenticationTerminalChat()
						?: terminalCertChat.copy().apply {
							readAccess.clear()
							writeAccess.clear()
							specialFunctions.clear()
						}

				val aad = eac1Input.authenticatedAuxiliaryData?.v?.toAuthenticatedAuxiliariyData()

				val paceDid = eac1InputReq.didName.didNameToPinId()

				val ctx =
					UiStepCtx(
						session,
						null,
						null,
						token,
						eserviceClient,
						eidServer,
						eac1InputReq,
						eac1Input,
						certs,
						terminalCert,
						certDesc,
						terminalCertChat,
						reqChat,
						optChat,
						aad,
						paceDid,
						terminalName,
					)
				return UiStepImpl(ctx)
			} catch (ex: Exception) {
				when (ex) {
					is TlvException,
					is NoSuchElementException,
					is IllegalArgumentException,
					-> throw InvalidServerData(eserviceClient, "Invalid data received in EAC1", cause = ex)
					else -> throw ex
				}
			}
		}

		private fun String.didNameToPinId(): PacePinId =
			when (this) {
				"PIN" -> PacePinId.PIN
				"CAN" -> PacePinId.CAN
				"PUK" -> PacePinId.PUK
				else -> throw IllegalArgumentException("Unknown DID name received from eID-Server")
			}
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun PrintableUByteArray.toAuthenticationTerminalChat(): AuthenticationTerminalChat {
	val chat = v.toTlvBer().tlv.toChat()
	return requireNotNull(
		chat.cast<AuthenticationTerminalChat>(),
	) { "CHAT in terminal certificate is of the wrong type" }
}
