package org.openecard.cif.bundled

import kotlinx.datetime.Instant
import org.openecard.cif.bundled.EgkCifDefinitions.autPace
import org.openecard.cif.bundled.EgkCifDefinitions.mrPinAmts
import org.openecard.cif.bundled.EgkCifDefinitions.mrPinAmtsRep
import org.openecard.cif.bundled.EgkCifDefinitions.mrPinDpe
import org.openecard.cif.bundled.EgkCifDefinitions.mrPinGdd
import org.openecard.cif.bundled.EgkCifDefinitions.mrPinHome
import org.openecard.cif.bundled.EgkCifDefinitions.mrPinNfd
import org.openecard.cif.bundled.EgkCifDefinitions.mrPinNfdRead
import org.openecard.cif.bundled.EgkCifDefinitions.mrPinOse
import org.openecard.cif.bundled.EgkCifDefinitions.pinCh
import org.openecard.cif.bundled.EgkCifDefinitions.pinQes
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_aut_e256
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_aut_r2048_signPKCS1_V1_5
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_aut_r2048_signPSS
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_autn_e256
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_autn_r2048_signPSS
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_enc_r2048
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_encv_r2048
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_qes_e256
import org.openecard.cif.bundled.EgkCifDefinitions.prk_ch_qes_r2048
import org.openecard.cif.bundled.GematikBuildingBlocks.alwaysAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.basePinParams
import org.openecard.cif.bundled.GematikBuildingBlocks.cmsProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.gematikCardCapabilities
import org.openecard.cif.bundled.GematikBuildingBlocks.mrPinHomePaceProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.neverAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.paceCmsProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.paceProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.pinChPaceProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.pinProtectedPaceAcl
import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.definition.did.PacePinId
import org.openecard.cif.definition.did.SignatureGenerationInfoType
import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.api.application.ApplicationScope
import org.openecard.cif.dsl.builder.CardInfoBuilder
import org.openecard.cif.dsl.builder.unaryPlus

@OptIn(ExperimentalUnsignedTypes::class)
val EgkCif by lazy {
	val b = CardInfoBuilder()

	b.metadata {
		id = "http://ws.gematik.de/egk/1.0.0"
		version = "1.0.0"
		status = CardInfoStatus.DEVELOPMENT
		name = "German Electronic eHealth Card"
		cardIssuer = "Gesellschaft für Telematikanwendungen der Gesundheitskarte mbH"
		creationDate = Instant.parse("2025-06-25T00:00:00Z")
		modificationDate = Instant.parse("2025-06-25T00:00:00Z")
	}

	b.capabilities {
		gematikCardCapabilities()
	}

	b.applications {
		add {
			appMf()
		}
		add {
			appDFHCA()
		}
		add {
			appDFNFD()
		}
		add {
			appDFDPE()
		}
		add {
			appDFGDD()
		}
		add {
			appDFOSE()
		}
		add {
			appDFAMTS()
		}
		add {
			appDFESIGN()
		}
		add {
			appDFQES()
		}
	}
	b.build()
}

object EgkCifDefinitions {
	val appMf = "MF"
	val appDFHCA = "DF.HCA"
	val appDFNFD = "DF.NFD"
	val appDFDPE = "DF.DPE"
	val appDFGDD = "DF.GDD"
	val appDFOSE = "DF.OSE"
	val appDFAMTS = "DF.AMTS"
	val appDFESIGN = "DF.ESIGN"
	val appDFQES = "DF.QES"

	val autPace = "AUT_PACE"
	val pinCh = "PIN.CH"
	val mrPinHome = "MRPIN.home"
	val mrPinNfd = "MRPIN.NFD"
	val mrPinDpe = "MRPIN.DPE"
	val mrPinGdd = "MRPIN.GDD"
	val mrPinNfdRead = "MRPIN.NFD_READ"
	val mrPinOse = "MRPIN.OSE"
	val mrPinAmts = "MRPIN.AMTS"
	val mrPinAmtsRep = "MRPIN.AMTS_REP"
	val prk_ch_aut_r2048_signPKCS1_V1_5 = "PrK.CH.AUT.R2048_signPKCS1_V1_5"
	val prk_ch_aut_r2048_signPSS = "PrK.CH.AUT.R2048_signPSS"
	val prk_ch_autn_r2048_signPSS = "PrK.CH.AUTN.R2048_signPSS"
	val prk_ch_enc_r2048 = "PrK.CH.ENC.R2048"
	val prk_ch_encv_r2048 = "PrK.CH.ENCV.R2048"
	val prk_ch_aut_e256 = "PrK.CH.AUT.E256"
	val prk_ch_autn_e256 = "PrK.CH.AUTN.E256"
	val pinQes = "PIN.QES"
	val prk_ch_qes_r2048 = "PrK.CH.QES.R2048"
	val prk_ch_qes_e256 = "PrK.CH.QES.E256"
}

private fun ApplicationScope.appMf() {
	name = EgkCifDefinitions.appMf
	aid = +"D2760001448000"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = "EF.ATR"
			description =
				"The transparent file EF.ATR contains information about the maximum size of the APDU. " +
				"It is also used to version variable elements of a map."
			path = +"2F01"
			shortEf = 0x1Du
			readAcl {
				alwaysAcl()
			}
			writeAcl {
				alwaysAcl()
			}
		}

		add {
			name = "EF.CardAccess"
			description =
				"EF.CardAccess is required for the PACE protocol when using the contactless interface."
			path = +"011C"
			shortEf = 0x1Cu
			readAcl {
				alwaysAcl()
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = "EF.C.CA.CS.E256"
			description =
				"This file contains a CV certificate for cryptography with elliptical curves, which contains " +
				"the public key PuK.CA.CS.E256 of a CA. This certificate can be checked by means of the " +
				"public key PuK.RCA.CS.E256."
			path = +"2F07"
			shortEf = 0x07u
			readAcl {
				paceCmsProtectedAcl()
			}
			writeAcl {
				cmsProtectedAcl()
			}
		}

		add {
			name = "EF.C.eGK.AUT_CVC.E256"
			description =
				"This file contains a CV certificate for cryptography with elliptical curves, which contains " +
				"the public key PuK.eGK.AUT-CVC.E256 to PrK.eGK.AUT-CVC.E256. This certificate can be " +
				"checked by means of the public key from EF.C.CA.CS.E256."
			path = +"2F06"
			shortEf = 0x06u
			readAcl {
				paceCmsProtectedAcl()
			}
			writeAcl {
				cmsProtectedAcl()
			}
		}

		add {
			name = "EF.DIR"
			description =
				"The EF.DIR file contains a list of application templates according to ISO7816-4. This list " +
				"is adjusted when the application structure changes by deleting or creating applications."
			path = +"2F00"
			shortEf = 0x1Eu
			readAcl {
				paceCmsProtectedAcl()
			}
			writeAcl {
				cmsProtectedAcl()
			}
		}

		add {
			name = "EF.GDO"
			description =
				"The ICCSN data object, which contains the identification number of the card, is stored in " +
				"EF.GDO. The identification number is based on [Resolution190]."
			path = +"2F02"
			shortEf = 0x02u
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = "EF.Version2"
			description =
				"""
				The file EF.Version2 contains the version numbers as well as product identifiers for variable elements of the card:
				- Version of the product type of the active object system (incl. cards)
				- Manufacturer-specific product identification of object system implementation
				- Versions of the filling rules for different files of this object system
				""".trimIndent()
			path = +"2F11"
			shortEf = 0x11u
			readAcl {
				alwaysAcl()
			}
			writeAcl {
				cmsProtectedAcl()
			}
		}
	}

	dids {
		pace {
			name = autPace
			scope = DidScope.GLOBAL
			authAcl {
				alwaysAcl()
			}
			parameters {
				passwordRef = PacePinId.CAN
				minLength = 6
				maxLength = 6
			}
		}

		pin {
			name = pinCh
			scope = DidScope.GLOBAL
			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}
			parameters {
				basePinParams()
				passwordRef = 0x01u
			}
		}

		pin {
			name = mrPinHome
			scope = DidScope.GLOBAL

			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x02u
			}
		}
		pin {
			name = mrPinNfd
			scope = DidScope.GLOBAL

			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x03u
			}
		}
		pin {
			name = mrPinDpe
			scope = DidScope.GLOBAL

			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x04u
			}
		}
		pin {
			name = mrPinGdd
			scope = DidScope.GLOBAL

			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x05u
			}
		}
		pin {
			name = mrPinNfdRead
			scope = DidScope.GLOBAL

			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x07u
			}
		}
		pin {
			name = mrPinOse
			scope = DidScope.GLOBAL

			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x09u
			}
		}
		pin {
			name = mrPinAmts
			scope = DidScope.GLOBAL

			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x0Cu
			}
		}
		pin {
			name = mrPinAmtsRep
			scope = DidScope.GLOBAL

			modifyAcl {
				pinChPaceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x0Du
			}
		}

// 				pin {
// 					name = "SK.CAN"
// 					scope = DidScope.GLOBAL
// 					modifyAcl {
// 						acl(CardProtocol.Any) {
// 							Never
// 						}
// 					}
// 					authAcl {
// 						acl(CardProtocol.Any) {
// 							Always
// 						}
// 					}
// 					parameters {
// 						passwordRef = 0x02u
// 						pwdType = PasswordType.ISO_9564_1
// 						minLength = 6
// 						maxLength = 8
// 					}
// 				}

		// RSAAuthMarker:
		// name: PrK.eGK.AUT_rsaRoleAuthentication
		// name: PrK.eGK.AUT_rsaSessionkey4SM
		// MutualAuthMarker:
		// name: SK.CMS
		// name: SK.VDS
		// name: SK.VSDCMS
	}

// 			elcRoleAuthentication, elcSessionkey4SM, elcAsynchronAdmin:
// 			PrK.eGK.AUT_CVC.E256

// 			Prüfung von CVC-Zertifikaten:
// 			PuK.RCA.CS.E256

// 			asymmetrische VSD/CMS-Authentisierung:
// 			PuK.RCA.ADMINCMS.CS.E256

// 			aesSessionkey4SM:
// 			SK.CMS.AES128
// 			SK.CMS.AES256
// 			SK.VSD.AES128
// 			SK.VSD.AES256
}

private fun ApplicationScope.appDFHCA() {
	name = EgkCifDefinitions.appDFHCA
	aid = +"D27600000102"

	selectAcl {
		alwaysAcl()
	}

	// add datasets

	dataSets {
		add {
			name = "EF.Einwilligung"
			description =
				"This file contains information about the consents for voluntary applications."
			path = +"D005"
			shortEf = 0x05u
			readAcl {
				mrPinHomePaceProtectedAcl()

// 						    CONTACT:
// 							PWD(MRPIN.home)
// 							OR   [PWD(PIN.CH)  AND   flagTI.25]
// 							CONTACTLESS
// 							AUT_PACE
// 							AND   { PWD(MRPIN.home)
// 								OR     [PWD(PIN.CH)   AND   flagTI.25] }
			}
			writeAcl {
				neverAcl()
// 							CONTACT:
// 							PWD(PIN.CH)   AND   flagTI.27
// 							CONTACTLESS:
// 							AUT_PACE
// 							AND   [PWD(PIN.CH)   AND   flagTI.27]
			}
		}

		add {
			name = "EF.GVD"
			description =
				"This file contains the protected insured person data. The details are described in Tab_eGK_ObjSys_035."
			path = +"D003"
			shortEf = 0x03u
			readAcl {
				mrPinHomePaceProtectedAcl()

// 							CONTACT:
// 							PWD(MRPIN.home)
// 							OR    [PWD(PIN.CH)   AND  flagTI.29]
// 							OR    flagTI.30
// 							OR    {AUT_VSD}

// 							CONTACTLESS:
// 							( AUT_PACE
// 								AND   { PWD(MRPIN.home)
// 								OR     [PWD(PacePinId.PIN.CH) AND flagTI.29]
// 								OR     flagTI.30 })
// 							OR      AUT_VSD
			}
			writeAcl {
				neverAcl()
				// AUT_VSD
			}
		}

		add {
			name = "EF.Logging"
			description =
				"This file contains logging information about access to the eHC."
			path = +"D006"
			shortEf = 0x06u
			readAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							PWD(MRPIN.home)
// 							OR   [PWD(PIN.CH)   AND   flagTI.33]

// 						CONTACTLESS:
// 							AUT_PACE
// 							AND   { PWD(MRPIN.home)
// 								OR   [ PWD(PIN.CH)   AND   flagTI.33] }
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = "EF.PD"
			description =
				"This file contains the cardholder's personal data."
			path = +"D001"
			shortEf = 0x01u
			readAcl {
				paceProtectedAcl()
// 							CONTACTLESS:
// 							OR AUT_VSD
			}
			writeAcl {
				neverAcl()
// 							 AUT_VSD
			}
		}

		add {
			name = "EF.Prüfungsnachweis"
			description =
				"This file stores a certificate that was created as part of an online check."
			path = +"D01C"
			shortEf = 0x1Cu
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				paceProtectedAcl()
			}
		}

		add {
			name = "EF.Standalone"
			description =
				"This file contains the information from EF.GVD and EF.DPE in encrypted form."
			path = +"DA0A"
			shortEf = 0x0Au
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				paceProtectedAcl()
			}
		}

		add {
			name = "EF.StatusVD"
			description =
				"This file contains information about the status of the data in EF.PD, EF.VD and EF.GVD."
			path = +"D00C"
			shortEf = 0x0Cu

			readAcl {
				paceProtectedAcl()
// 						CONTACTLESS:
// 							AUT_PACE
// 							OR     AUT_VSD
			}
			writeAcl {
				neverAcl()
// 							 AUT_VSD
			}
		}

		add {
			name = "EF.VD"
			description =
				"This file contains the insured person data."
			path = +"D002"
			shortEf = 0x02u
			readAcl {
				paceProtectedAcl()
// 							CONTACTLESS:
// 							AUT_PACE
// 							OR    AUT_VSD
			}
			writeAcl {
				neverAcl()
// 							 AUT_VSD
			}
		}

		add {
			name = "EF.Verweis"
			description =
				"This file contains information about the storage locations of the data of the voluntary applications that are not stored on the eHC."
			path = +"D009"
			shortEf = 0x09u
			readAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							PWD(MRPIN.home)
// 							OR   [PWD(PIN.CH)   AND   flagTI.28]
// 						CONTACTLESS:
// 						AUT_PACE
// 						AND   { PWD(MRPIN.home)
// 							OR     [ PWD(PIN.CH)   AND   flagTI.28 ] }
			}
			writeAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							PWD(MRPIN.home)
// 							OR   [PWD(PIN.CH)   AND   flagTI.28]
// 						CONTACTLESS:
// 						AUT_PACE
// 						AND   { PWD(MRPIN.home)
// 							OR     [ PWD(PIN.CH)   AND   flagTI.28 ] }
			}
		}
	}
}

private fun ApplicationScope.appDFNFD() {
	name = EgkCifDefinitions.appDFNFD
	aid = +"D27600014407"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = "EF.NFD"
			description = "This file contains an emergency data record."
			path = +"D010"
			shortEf = 0x10u
			readAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{ activeDidState("flagTI.18") },
// 								{
// 									and(
// 										activeDidState("MRPIN.NFD_READ"),
// 										activeDidState("flagTI.17"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.17"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 							CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.18"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.NFD.READ"),
// 										activeDidState("flagTI.17"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.17"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
			writeAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("flagTI.15"),
// 										activeDidState("MRPIN.NFD"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.15"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.NFD"),
// 										activeDidState("flagTI.15"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.15"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}

		add {
			name = "EF.StatusNFD"
			description = "This file contains information about the status of the emergency data record."
			path = +"D00E"
			shortEf = 0x0Eu
			readAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("flagTI.18")
// 								},
// 								{
// 									and(
// 										activeDidState("MRPIN.NFD_READ"),
// 										activeDidState("flagTI.17"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.17"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.18"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.NFD_READ"),
// 										activeDidState("flagTI.17"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.17"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

			writeAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRPIN.NFD"),
// 										activeDidState("flagTI.15"),
// 									)
// 								},
// 								{
// 									and(
// 									activeDidState("PIN.CH"),
// 										activeDidState("flagTI.15"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.NFD"),
// 										activeDidState("flagTI.15"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.15"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}
	}
}

private fun ApplicationScope.appDFDPE() {
	name = EgkCifDefinitions.appDFDPE
	aid = +"D27600014408"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = "EF.DPE"
			description =
				"This file contains the data record with the personal declarations of the insured person."
			path = +"D01B"
			shortEf = 0x1Bu

			readAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{ activeDidState("MRPIN.home") },
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 								{ activeDidState("flagTI.23") },

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.23"),
// 									)
// 								},
			}
			writeAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRPIN.DPE"),
// 										activeDidState("flagTI.20"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.DPE"),
// 										activeDidState("flagTI.20"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}

		add {
			name = "EF.StatusDPE"
			description =
				"This file contains information on the status of the data record with the personal declarations."
			path = +"D018"
			shortEf = 0x18u
			readAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{ activeDidState("MRPIN.home") },
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 								{ activeDidState("flagTI.23") },
// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.23"),
// 									)
// 								},
// 						)
			}
			writeAcl {
				neverAcl()
// 					CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRPIN.DPE"),
// 										activeDidState("flagTI.20"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 					CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.DPE"),
// 										activeDidState("flagTI.20"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}
	}
}

private fun ApplicationScope.appDFGDD() {
	name = EgkCifDefinitions.appDFGDD
	aid = +"D2760001440A"

	selectAcl {
		acl(CardProtocol.Any) {
			Always
		}
	}

	dataSets {
		add {
			name = "EF.EinwilligungGDD"
			description =
				"This file contains information about the consents to voluntary applications of health data services."
			path = +"D013"
			shortEf = 0x13u
			readAcl {
				mrPinHomePaceProtectedAcl()
// 					CONTACT:
// 						or(
// 							{
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("MRPIN.GDD"),
// 										activeDidState("flagTI.40"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)

// 					CONTACTLESS:
// 						or(
// 							{
// 								activeDidState("AUT_PACE")
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.GDD"),
// 										activeDidState("flagTI.40"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)
			}
			writeAcl {
				mrPinHomePaceProtectedAcl()
// 					CONTACT:
// 						or(
// 							{
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("MRPIN.GDD"),
// 										activeDidState("flagTI.40"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)

// 					CONTACTLESS:
// 						or(
// 							{
// 								activeDidState("AUT_PACE")
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.GDD"),
// 										activeDidState("flagTI.40"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)
			}
		}

		add {
			name = "EF.VerweiseGDD"
			description =
				"This file contains information on the storage locations of the data of the voluntary health data services applications that are not stored on the eHC."
			path = +"D01A"
			shortEf = 0x1Au
			readAcl {
				mrPinHomePaceProtectedAcl()
// 					CONTACT:
// 						or(
// 							{
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("MRPIN.GDD"),
// 										activeDidState("flagTI.40"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)

// 					CONTACTLESS:
// 						or(
// 							{
// 								activeDidState("AUT_PACE")
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.GDD"),
// 										activeDidState("flagTI.40"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)
			}
			writeAcl {
				mrPinHomePaceProtectedAcl()
// 					CONTACT:
// 						or(
// 							{
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("MRPIN.GDD"),
// 										activeDidState("flagTI.40"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)

// 					CONTACTLESS:
// 						or(
// 							{
// 								activeDidState("AUT_PACE")
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.GDD"),
// 										activeDidState("flagTI.40"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)
			}
		}
	}
}

private fun ApplicationScope.appDFOSE() {
	name = EgkCifDefinitions.appDFOSE
	aid = +"D2760001440B"

	selectAcl {
		acl(CardProtocol.Any) {
			Always
		}
	}

	dataSets {
		add {
			name = "EF.OSE"
			description =
				"This file contains a data record for the organ donation declaration."
			path = +"E001"
			shortEf = 0x01u
			readAcl {
				mrPinHomePaceProtectedAcl()
// 					CONTACT:
// 						or(
// 							{
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									activeDidState("flagTI.42")
// 								},
// 								{
// 									and(
// 										activeDidState("MRPIN.OSE"),
// 										activeDidState("flagTI.41"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)

// 					CONTACTLESS:
// 						or(
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("MRPIN.home"),
// 								)
// 							},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.42"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.OSE"),
// 										activeDidState("flagTI.41"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)
			}
			writeAcl {
				neverAcl()
// 					CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRPIN.OSE"),
// 										activeDidState("flagTI.43"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 					CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.OSE"),
// 										activeDidState("flagTI.43"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}

		add {
			name = "EF.StatusOSE"
			description =
				"This file contains information on the status of the organ donation declaration."
			path = +"E002"
			shortEf = 0x02u

			readAcl {
				mrPinHomePaceProtectedAcl()
// 					CONTACT:
// 						or(
// 							{
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									activeDidState("flagTI.42")
// 								},
// 								{
// 									and(
// 										activeDidState("MRPIN.OSE"),
// 										activeDidState("flagTI.41"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)

// 					CONTACTLESS:
// 						or(
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("MRPIN.home"),
// 								)
// 							},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.42"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 						)
			}
			writeAcl {
				neverAcl()
// 					CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRPIN.OSE"),
// 										activeDidState("flagTI.43"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 					CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.OSE"),
// 										activeDidState("flagTI.43"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}
	}
}

private fun ApplicationScope.appDFAMTS() {
	name = EgkCifDefinitions.appDFAMTS
	aid = +"D2760001440C"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = "EF.AMTS"
			description =
				"This file contains a data set for AMTS data management."
			path = +"E005"
			shortEf = 0x05u
			readAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRIN.AMTS"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.AMTS_REP"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.46"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.AMTS_REP"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.46"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
			writeAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("MRPIN.AMTS_REP"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.47"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.AMTS_REP"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.47"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}

		add {
			name = "EF.VerweiseAMTS"
			description =
				"This file contains information on the storage locations of data from the voluntary AMTS data management application that is not stored on the eHC."
			path = +"E006"
			shortEf = 0x06u
			readAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("MRPIN.home")
// 								},
// 								{
// 									and(
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.AMTS_REP"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.46"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.AMTS_REP"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.46"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
			writeAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.47"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.47"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}

		add {
			name = "EF.StatusAMTS"
			description =
				"This file contains information on the status of the AMTS Data Management application."
			path = +"E007"
			shortEf = 0x07u
			readAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRIN.AMTS"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.AMTS_REP"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.46"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.AMTS_REP"),
// 										activeDidState("flagTI.46"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.46"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
			writeAcl {
				neverAcl()
// 						CONTACT:
// 							or(
// 								{
// 									and(
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("MRPIN.AMTS_REP"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.47"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.AMTS"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.AMTS_REP"),
// 										activeDidState("flagTI.47"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.47"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
			}
		}
	}
}

private fun ApplicationScope.appDFESIGN() {
	name = EgkCifDefinitions.appDFESIGN
	aid = +"A000000167455349474E"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = "EF.C.CH.AUT.R2048"
			description =
				"This file contains a certificate for cryptography with RSA with the public key PuK.CH.AUT.R2048 to PrK.CH.AUT.R2048."
			path = +"C500"
			shortEf = 0x01u

			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
// 						 AUT_CMS
			}
		}

		add {
			name = "EF.C.CH.AUTN.R2048"
			description =
				"This file contains a certificate for cryptography with RSA with the public key PuK.CH.AUTN.R2048 to PrK.CH.AUTN.R2048."
			path = +"C509"
			shortEf = 0x09u

			readAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("MRPIN.home")
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.8"),
// 									)
// 								},
// 								{
// 									activeDidState("flagTI.9")
// 								},
// 								{
// 									activeDidState("AUT_CMS")
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.8"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.9"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("AUT_CMS"),
// 									)
// 								},
// 							)
			}
			writeAcl {
				neverAcl()
// 						AUT_CMS
			}
		}
		add {
			name = " EF.C.CH.ENC.R2048"
			description =
				"This file contains a certificate for cryptography with RSA with the public key PuK.CH.ENC.R2048 to PrK.CH.ENC.R2048."
			path = +"C200"
			shortEf = 0x02u

			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = " EF.C.CH.ENCV.R2048"
			description =
				"This file contains a certificate for cryptography with RSA with the public key PuK.CH.ENCV.R2048 to PrK.CH.ENCV.R2048."
			path = +"C50A"
			shortEf = 0x0Au

			readAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("MRPIN.home")
// 								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.10"),
// 									)
// 								},
// 								{
// 									activeDidState("flagTI.11")
// 								},
// 								{
// 									activeDidState("AUT_CMS")
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.10"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.11"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("AUT_CMS"),
// 									)
// 								},
// 							)
			}
			writeAcl {
				neverAcl()
// 						AUT_CMS
			}
		}
		add {
			name = "EF.C.CH.AUT.E256"
			description =
				"This file contains an X.509 authentication certificate for elliptic curve cryptography with the public key PuK.CH.AUT.E256 to PrK.CH.AUT.E256."
			path = +"C504"
			shortEf = 0x04u

			readAcl {
				paceProtectedAcl()
// 						CONTACTLESS:
// 						OR AUT_CMS
			}

			writeAcl {
				neverAcl()
// 						AUT_CMS
			}
		}
		add {
			name = "EF.C.CH.AUTN.E256"
			description =
				"This file contains an X.509 authentication certificate for elliptic curve cryptography with the public key PuK.CH.AUT.E256 to PrK.CH.AUT.E256."
			path = +"C50B"
			shortEf = 0x0Bu

			readAcl {
				mrPinHomePaceProtectedAcl()
// 					CONTACT:
// 						or(
// 							{
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.8"),
// 									)
// 								},
// 								{
// 									activeDidState("flagTI.9")
// 								},
// 								{
// 									activeDidState("AUT_CMS")
// 								},
// 						)

// 					CONTACTLESS:
// 						or(
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("MRPIN.home"),
// 								)
// 							},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.8"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.9"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("AUT_CMS"),
// 									)
// 								},
// 						)
			}

			writeAcl {
				neverAcl()
// 						AUT_CMS
			}
		}
		add {
			name = "EF.C.CH.ENC.E256"
			description =
				"This file contains an encryption certificate for elliptic curve cryptography with the public key PuK.CH.ENC.E256 to PrK.CH.ENC.E256."
			path = +"C205"
			shortEf = 0x05u

			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = "EF.C.CH.ENCV.E256"
			description =
				"This file contains an encryption certificate for elliptic curve cryptography with the public key PuK.CH.ENCV.E256 to PrK.CH.ENCV.E256."
			path = +"C50C"
			shortEf = 0x0Cu

			readAcl {
				mrPinHomePaceProtectedAcl()
// 					CONTACT:
// 						or(
// 							{
// 								activeDidState("MRPIN.home")
// 							},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.10"),
// 									)
// 								},
// 								{
// 									activeDidState("flagTI.11")
// 								},
// 								{
// 									activeDidState("AUT_CMS")
// 								},
// 						)

// 					CONTACTLESS:
// 						or(
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("MRPIN.home"),
// 								)
// 							},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.10"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.11"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("AUT_CMS"),
// 									)
// 								},
// 						)
			}
			writeAcl {
				neverAcl()
// 						AUT_CMS
			}
		}
	}

	dids {
		signature {
			name = prk_ch_aut_r2048_signPKCS1_V1_5
			scope = DidScope.GLOBAL

			signAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("MRPIN.home")
// 								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
// 							)

// 						CONTACTLESS:

// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
// 							)
			}

			parameters {
				key {
					keyRef = 0x02u
					keySize = 2048
				}

				certificates("EF.C.CH.AUT.R2048")

				signatureAlgorithm = "SHA256withRSA"

				sigGen {
					standard {
						cardAlgRef = +"02"
						info(
							SignatureGenerationInfoType.MSE_KEY_DS,
							SignatureGenerationInfoType.PSO_CDS,
						)
					}
				}
			}
		}

		signature {
			name = prk_ch_aut_r2048_signPSS
			scope = DidScope.GLOBAL

			signAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("MRPIN.home")
// 								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
// 							)
			}

			parameters {
				key {
					keyRef = 0x02u
					keySize = 2048
				}

				certificates("EF.C.CH.AUT.R2048")
				signatureAlgorithm = "SHA256withRSAandMGF1"
				sigGen {
					standard {
						cardAlgRef = +"05"
						info(
							SignatureGenerationInfoType.MSE_KEY_DS,
							SignatureGenerationInfoType.PSO_CDS,
						)
					}
				}
			}
		}

		signature {
			name = prk_ch_autn_r2048_signPSS
			scope = DidScope.GLOBAL

			signAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("MRPIN.home")
// 								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.8")
// 								)
// 							},
// 								{
// 									activeDidState("flagTI.9")
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.8"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.9"),
// 									)
// 								},
// 							)
			}

			parameters {
				key {
					keyRef = 0x06u
					keySize = 2048
				}
				certificates("EF.C.CH.AUTN.R2048")
				signatureAlgorithm = "SHA256withRSAandMGF1"

				sigGen {
					standard {
						cardAlgRef = +"05"
						info(
							SignatureGenerationInfoType.MSE_KEY_DS,
							SignatureGenerationInfoType.PSO_CDS,
						)
					}
				}
			}
		}

		encrypt {
			name = prk_ch_enc_r2048
			scope = DidScope.LOCAL

			encipherAcl {
				neverAcl()
			}

			parameters {
				key {
					keyRef = 0x03u
					keySize = 2048
				}

				encryptionAlgorithm = "RSA/NONE/OAEPWithSHA256AndMGF1Padding"
				certificates("EF.C.CH.ENC.R2048")
				cardAlgRef = +"85"
			}
		}

		encrypt {
			name = prk_ch_encv_r2048
			scope = DidScope.LOCAL

			encipherAcl {
				neverAcl()
			}

			parameters {
				key {
					keyRef = 0x07u
					keySize = 2048
				}

				encryptionAlgorithm = "RSA/NONE/OAEPWithSHA256AndMGF1Padding"
				certificates("EF.C.CH.ENCV.R2048")
				cardAlgRef = +"85"
			}
		}

		signature {
			name = prk_ch_aut_e256
			scope = DidScope.GLOBAL

			signAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("MRPIN.home")
// 								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
// 							)
// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
// 							)
			}

			parameters {
				key {
					keyRef = 0x04u
					keySize = 2048
				}

				certificates("EF.C.CH.AUT.E256")

				signatureAlgorithm = "SHA256withECDSA"

				sigGen {
					standard {
						cardAlgRef = +"00"
						info(
							SignatureGenerationInfoType.MSE_KEY_DS,
							SignatureGenerationInfoType.PSO_CDS,
						)
					}
				}
			}
		}

		signature {
			name = prk_ch_autn_e256
			scope = DidScope.GLOBAL

			signAcl {
				mrPinHomePaceProtectedAcl()
// 						CONTACT:
// 							or(
// 								{
// 									activeDidState("MRPIN.home")
// 								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.8")
// 								)
// 							},
// 								{
// 									activeDidState("flagTI.9")
// 								},
// 							)

// 						CONTACTLESS:
// 							or(
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("MRPIN.home"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.8"),
// 									)
// 								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("flagTI.9"),
// 									)
// 								},
// 							)
			}

			parameters {
				key {
					keyRef = 0x0Bu
					keySize = 2048
				}
				certificates("EF.C.CH.AUTN.E256")
				signatureAlgorithm = "SHA256withECDSA"

				sigGen {
					standard {
						cardAlgRef = +"00"
						info(
							SignatureGenerationInfoType.MSE_KEY_DS,
							SignatureGenerationInfoType.PSO_CDS,
						)
					}
				}
			}
		}
	}
}

private fun ApplicationScope.appDFQES() {
	name = EgkCifDefinitions.appDFQES
	aid = +"D27600006601"

	description = "Optional QES application"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = "EF.C.CH.QES.R2048"
			description =
				"This file contains a certificate for cryptography with RSA with the public key PuK.CH.QES.R2048 to PrK.CH.QES.R2048."
			path = +"C000"
			shortEf = 0x10u

			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
				// manufacturer-specific
			}
		}

		add {
			name = "EF.C.CH.QES.E256"
			description =
				"This file contains a certificate for cryptography with elliptic curves with the public key PuK.CH.QES.E256 to PrK.CH.QES.E256."
			path = +"C006"
			shortEf = 0x06u

			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
				// manufacturer-specific
			}
		}
	}

	dids {
		pin {
			name = pinQes
			scope = DidScope.GLOBAL

			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x01u
			}
		}

		signature {
			name = prk_ch_qes_r2048
			scope = DidScope.GLOBAL

			signAcl {
				pinProtectedPaceAcl("PIN.QES")
			}

			parameters {
				key {
					keyRef = 0x04u
					keySize = 2048
				}

				signatureAlgorithm = "SHA256withRSAandMGF1"
				sigGen {
					standard {
						cardAlgRef = +"05"
						info(
							SignatureGenerationInfoType.MSE_KEY_DS,
							SignatureGenerationInfoType.PSO_CDS,
						)
					}
				}
			}
		}

		signature {
			name = prk_ch_qes_e256
			scope = DidScope.GLOBAL

			signAcl {
				pinProtectedPaceAcl("PIN.QES")
			}

			parameters {
				key {
					keyRef = 0x06u
					keySize = 2048
				}

				signatureAlgorithm = "SHA256withECDSA"

				sigGen {
					standard {
						cardAlgRef = +"00"
						info(
							SignatureGenerationInfoType.MSE_KEY_DS,
							SignatureGenerationInfoType.PSO_CDS,
						)
					}
				}
			}
		}
	}
}
