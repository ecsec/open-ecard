package org.openecard.cif.bundled

import kotlinx.datetime.Instant
import org.openecard.cif.bundled.GematikBuildingBlocks.alwaysAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.basePinParams
import org.openecard.cif.bundled.GematikBuildingBlocks.basePukParams
import org.openecard.cif.bundled.GematikBuildingBlocks.cmsCupProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.cmsProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.neverAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.paceCmsCupProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.paceCmsProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.paceProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.pinChPaceProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.pinProtectedPaceAcl
import org.openecard.cif.bundled.HbaDefinitions.Apps.Auto
import org.openecard.cif.bundled.HbaDefinitions.Apps.Auto.Datasets.ef_c_hp_auto1_r3072
import org.openecard.cif.bundled.HbaDefinitions.Apps.Auto.Datasets.ef_c_hp_auto2_r3072
import org.openecard.cif.bundled.HbaDefinitions.Apps.Auto.Dids.pinAuto
import org.openecard.cif.bundled.HbaDefinitions.Apps.Auto.Dids.pinSo
import org.openecard.cif.bundled.HbaDefinitions.Apps.Auto.Dids.prk_hp_auto_r3072_signPKCS1_V1_5
import org.openecard.cif.bundled.HbaDefinitions.Apps.Auto.Dids.prk_hp_auto_r3072_signPSS
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaEsign
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaEsign.Datasets.cia_esign_efAod
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaEsign.Datasets.cia_esign_efCd
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaEsign.Datasets.cia_esign_efOd
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaEsign.Datasets.cia_esign_efPrkd
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaEsign.Datasets.cia_esign_ef_cia_ciaInfo
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaQes
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaQes.Datasets.cia_qes_efAod
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaQes.Datasets.cia_qes_efCd
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaQes.Datasets.cia_qes_efOd
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaQes.Datasets.cia_qes_efPrkd
import org.openecard.cif.bundled.HbaDefinitions.Apps.CiaQes.Datasets.cia_qes_ef_cia_ciaInfo
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Datasets.ef_c_hp_aut_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Datasets.ef_c_hp_aut_r2048
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Datasets.ef_c_hp_enc_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Datasets.ef_c_hp_enc_r2048
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Datasets.ef_c_hp_sig_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Datasets.ef_c_hp_sig_r2048
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Dids.prk_hp_aut_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Dids.prk_hp_aut_r2048_signPKCS1_V1_5
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Dids.prk_hp_aut_r2048_signPSS
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Dids.prk_hp_enc_r2048
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Dids.prk_hp_sig_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.ESign.Dids.prk_hp_sig_r2048
import org.openecard.cif.bundled.HbaDefinitions.Apps.Hpa
import org.openecard.cif.bundled.HbaDefinitions.Apps.Hpa.Datasets.efHpd
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Datasets.efAtr
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Datasets.efCardAccess
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Datasets.efDir
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Datasets.efGdo
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Datasets.efVersion2
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Datasets.ef_c_ca_cs_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Datasets.ef_c_hpc_autd_suk_cvc_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Datasets.ef_c_hpc_autr_cvc_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Dids.autPace
import org.openecard.cif.bundled.HbaDefinitions.Apps.MF.Dids.pinCh
import org.openecard.cif.bundled.HbaDefinitions.Apps.Qes
import org.openecard.cif.bundled.HbaDefinitions.Apps.Qes.Datasets.efSsec
import org.openecard.cif.bundled.HbaDefinitions.Apps.Qes.Datasets.ef_c_hp_qes_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.Qes.Datasets.ef_c_hp_qes_r2048
import org.openecard.cif.bundled.HbaDefinitions.Apps.Qes.Dids.pinQes
import org.openecard.cif.bundled.HbaDefinitions.Apps.Qes.Dids.prk_hp_qes_e256
import org.openecard.cif.bundled.HbaDefinitions.Apps.Qes.Dids.prk_hp_qes_r2048
import org.openecard.cif.definition.capabilities.CommandCodingDefinitions
import org.openecard.cif.definition.dataset.DatasetType
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.definition.did.PacePinId
import org.openecard.cif.definition.did.PasswordFlags
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.definition.did.SignatureGenerationInfoType
import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.api.application.ApplicationScope
import org.openecard.cif.dsl.builder.CardInfoBuilder
import org.openecard.cif.dsl.builder.unaryPlus

val HbaCif by lazy {
	val b = CardInfoBuilder()

	b.metadata {
		id = HbaDefinitions.cardType
		version = "1.0.0"
		status = CardInfoStatus.DEVELOPMENT
		name = "HPC-G2"
		cardIssuer = "Bundesärztekammer"
		creationDate = Instant.parse("2025-06-25T00:00:00Z")
		modificationDate = Instant.parse("2025-06-25T00:00:00Z")
	}

	b.capabilities {
		selectionMethods {
			selectDfByFullName = true
			selectDfByPartialName = false
			selectDfByPath = false
			selectDfByFileId = true
			selectDfImplicit = false
			supportsShortEf = true
			supportsRecordNumber = true
			supportsRecordIdentifier = false
		}

		dataCoding {
			tlvEfs = false
			writeOneTime = false
			writeProprietary = true
			writeOr = false
			writeAnd = false
			ffValidAsTlvFirstByte = false
			dataUnitsQuartets = 2
		}

		commandCoding {
			supportsCommandChaining = true
			supportsExtendedLength = true
			logicalChannel = CommandCodingDefinitions.LogicalChannelAssignment.ASSIGN_BY_CARD
			maximumLogicalChannels = 3
		}
	}

	b.applications {
		add {
			appMf()
		}
		add {
			appDFHPA()
		}
		add {
			appDFQES()
		}
		add {
			appDFESIGN()
		}
		add {
			appDFCIAQES()
		}
		add {
			appDFCIAESIGN()
		}
		add {
			appDFAUTO()
		}
	}
	b.build()
}

object HbaDefinitions {
	val cardType = "http://www.baek.de/cif/HPC-G2"

	object Apps {
		object MF {
			val name = "MF"

			object Datasets {
				val efAtr = "EF.ATR"
				val efCardAccess = "EF.CardAccess"
				val efDir = "EF.DIR"
				val efGdo = "EF.GDO"
				val efVersion2 = "EF.Version2"
				val ef_c_ca_cs_e256 = "EF.C.CA.CS.E256"
				val ef_c_hpc_autr_cvc_e256 = "EF.C.HPC.AUTR_CVC.E256"
				val ef_c_hpc_autd_suk_cvc_e256 = "EF.C.HPC.AUTD_SUK_CVC.E256"
			}

			object Dids {
				val autPace = GematikBuildingBlocks.autPace
				val pinCh = GematikBuildingBlocks.pinCh
			}
		}

		object Hpa {
			val name = "DF.HPA"

			object Datasets {
				val efHpd = "EF.HPD"
			}
		}

		object Qes {
			val name = "DF.QES"

			object Datasets {
				val ef_c_hp_qes_r2048 = "EF.C.HP.QES.R2048"
				val ef_c_hp_qes_e256 = "EF.C.HP.QES.E256"
				val efSsec = "EF.SSEC"
			}

			object Dids {
				val pinQes = "PIN.QES"
				val prk_hp_qes_r2048 = "PrK.HP.QES.R2048"
				val prk_hp_qes_e256 = "PrK.HP.QES.E256"
			}
		}

		object ESign {
			val name = "DF.ESIGN"

			object Datasets {
				val ef_c_hp_aut_r2048 = "EF.C.HP.AUT.R2048"
				val ef_c_hp_enc_r2048 = "EF.C.HP.ENC.R2048"
				val ef_c_hp_aut_e256 = "EF.C.HP.AUT.E256"
				val ef_c_hp_enc_e256 = "EF.C.HP.ENC.E256"
				val ef_c_hp_sig_r2048 = "EF.C.HP.SIG.R2048"
				val ef_c_hp_sig_e256 = "EF.C.HP.SIG.E256"
			}

			object Dids {
				val prk_hp_aut_r2048_signPKCS1_V1_5 = "PrK.HP.AUT.R2048_signPKCS1_V1_5"
				val prk_hp_aut_r2048_signPSS = "PrK.HP.AUT.R2048_signPSS"
				val prk_hp_enc_r2048 = "PrK.HP.ENC.R2048"
				val prk_hp_aut_e256 = "PrK.HP.AUT.E256"
				val prk_hp_sig_r2048 = "PrK.HP.SIG.R2048"
				val prk_hp_sig_e256 = "PrK.HP.SIG.E256"
			}
		}

		object CiaQes {
			val name = "DF.CIA.QES"

			object Datasets {
				val cia_qes_ef_cia_ciaInfo = "DF.CIA.QES/EF.CIA.CIAInfo"
				val cia_qes_efOd = "DF.CIA.QES/EF.OD"
				val cia_qes_efAod = "DF.CIA.QES/EF.AOD"
				val cia_qes_efPrkd = "DF.CIA.QES/EF.PrKD"
				val cia_qes_efCd = "DF.CIA.QES/EF.CD"
			}
		}

		object CiaEsign {
			val name = "DF.CIA.ESIGN"

			object Datasets {
				val cia_esign_ef_cia_ciaInfo = "DF.CIA.ESIGN/EF.CIA.CIAInfo"
				val cia_esign_efOd = "DF.CIA.ESIGN/EF.OD"
				val cia_esign_efAod = "DF.CIA.ESIGN/EF.AOD"
				val cia_esign_efPrkd = "DF.CIA.ESIGN/EF.PrKD"
				val cia_esign_efCd = "DF.CIA.ESIGN/EF.CD"
			}
		}

		object Auto {
			val name = "DF.AUTO"

			object Datasets {
				val ef_c_hp_auto1_r3072 = "EF.C.HP.AUTO1.R3072"
				val ef_c_hp_auto2_r3072 = "EF.C.HP.AUTO2.R3072"
			}

			object Dids {
				val pinAuto = "PIN.AUTO"
				val pinSo = "PIN.SO"
				val prk_hp_auto_r3072_signPKCS1_V1_5 = "PrK.HP.AUTO.R3072_signPKCS1_V1_5"
				val prk_hp_auto_r3072_signPSS = "PrK.HP.AUTO.R3072_signPSS"
			}
		}
	}
}

private fun ApplicationScope.appMf() {
	name = MF.name
	aid = +"D2760001448000"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = efAtr
			description =
				"The transparent file EF.ATR contains information about the maximum size of the APDU. " +
				"It is also used to version variable elements of a map."
			path = +"2F01"
			shortEf = 0x1Du
			type = DatasetType.TRANSPARENT
			readAcl {
				alwaysAcl()
			}
			writeAcl {
				alwaysAcl()
			}
		}

		add {
			name = efCardAccess
			description =
				"EF.CardAccess is required for the PACE protocol when using the contactless interface."
			path = +"011C"
			shortEf = 0x1Cu
			type = DatasetType.TRANSPARENT
			readAcl {
				alwaysAcl()
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = efDir
			description =
				"The EF.DIR file contains a list of application templates according to ISO7816-4. This list " +
				"is adjusted when the application structure changes by deleting or creating applications."
			path = +"2F00"
			shortEf = 0x1Eu
			type = DatasetType.RECORD
			readAcl {
				paceCmsProtectedAcl()
			}
			writeAcl {
				cmsProtectedAcl()
			}
		}

		add {
			name = efGdo
			description =
				"The ICCSN data object, which contains the identification number of the card, is stored in " +
				"EF.GDO. The identification number is based on [Resolution190]."
			path = +"2F02"
			shortEf = 0x02u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = efVersion2
			description =
				"""
				The file EF.Version2 contains the version numbers as well as product identifiers for variable elements of the card:
				- Version of the product type of the active object system (incl. cards)
				- Manufacturer-specific product identification of object system implementation
				- Versions of the filling rules for different files of this object system
				""".trimIndent()
			path = +"2F11"
			shortEf = 0x11u
			type = DatasetType.TRANSPARENT
			readAcl {
				alwaysAcl()
			}

			writeAcl {
				cmsProtectedAcl()
			}
		}

		add {
			name = ef_c_ca_cs_e256
			description =
				"This file contains a CV certificate for cryptography with elliptical curves, which contains " +
				"the public key PuK.CA.CS.E256 of a CA. This certificate can be checked by means of the " +
				"public key PuK.RCA.CS.E256."
			path = +"2F07"
			shortEf = 0x07u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceCmsCupProtectedAcl()
			}
			writeAcl {
				cmsCupProtectedAcl()
			}
		}

		add {
			name = ef_c_hpc_autr_cvc_e256
			description =
				"EF.C.HPC.AUTR_CVC.E256 contains the CV certificate of the HBA for cryptography with elliptic curves " +
				"for role-based C2C authentication between HBA and eGK and for the authorization of the SMC-B. " +
				"This certificate can be checked using the public key from EF.C.CA.CS.E256 (see Tab_HBA_ObjSys_011)."
			path = +"2F06"
			shortEf = 0x06u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceCmsCupProtectedAcl()
			}
			writeAcl {
				cmsCupProtectedAcl()
			}
		}

		add {
			name = ef_c_hpc_autd_suk_cvc_e256
			description =
				"EF.C.HPC.AUTD_SUK_CVC.E256 contains the CV certificate of the HBA for cryptography with elliptic curves for " +
				"function-based C2C authentication between HBA/gSMC-KT and HBA/gSMC-K with the HBA as a signature card " +
				"for batch and convenience signatures (SUK) in order to receive PIN data and the data to be signed (DTBS)."
			path = +"2F09"
			shortEf = 0x09u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceCmsCupProtectedAcl()
			}
			writeAcl {
				cmsCupProtectedAcl()
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
				maxLength = 8
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
			resetAcl {
				paceProtectedAcl()
			}
			parameters {
				basePinParams()
				passwordRef = 0x01u
				pwdFlags =
					setOf(
						PasswordFlags.MODIFY_WITH_OLD_PASSWORD,
						PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK_AND_PASSWORD,
						PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK,
					)
				unblockingPassword {
					basePukParams()
				}
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

// 				elcRoleAuthentication:
// 				PrK.HPC.AUTR_CVC.E256
// 				elcSessionkey4SM, elcAsynchronAdmin:
// 				PrK.HPC.AUTD_SUK_CVC.E256

// 				Prüfung von CVC-Zertifikaten:
// 				PuK.RCA.CS.E256

// 				asymmetrische CMS-Authentisierung:
// 				PuK.RCA.ADMINCMS.CS.E256

// 				aesSessionkey4SM
// 				SK.CMS.AES128
// 				SK.CMS.AES256
// 				SK.CUP.AES128
// 				SK.CUP.AES256
	}
}

private fun ApplicationScope.appDFHPA() {
	name = Hpa.name
	aid = +"D27600014602"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = efHpd
			description =
				"The transparent EF.HPD file is intended for storing data relating to the respective healthcare professional, " +
				"e.g. confirmation of participation in further training measures. The file can always be read, but an update " +
				"is only possible after the PIN.CH has been successfully entered."
			path = +"D001"
			shortEf = 0x01u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				pinProtectedPaceAcl(pinCh)
			}
		}
	}
}

private fun ApplicationScope.appDFQES() {
	name = Qes.name
	aid = +"D27600006601"
	description = "Optional QES application"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = ef_c_hp_qes_r2048
			description =
				"The transparent file EF.C.HP.QES.R2048 contains the X.509 certificate for cryptography with RSA with the " +
				"public key of the healthcare professional PuK.HP.QES.R2048 for the qualified electronic signature in " +
				"accordance with EU Regulation No. 910/2014 (eIDAS)."
			path = +"C000"
			shortEf = 0x10u
			type = DatasetType.TRANSPARENT

			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
				// manufacturer-specific
			}
		}

		add {
			name = ef_c_hp_qes_e256
			description =
				"The transparent file EF.C.HP.QES.E256 contains the X.509 certificate for cryptography with elliptic curves " +
				"with the public key of the healthcare professional PuK.HP.QES.E256 for the qualified electronic signature " +
				"in accordance with EU Regulation No. 910/2014 (eIDAS)."
			path = +"C006"
			shortEf = 0x06u
			type = DatasetType.TRANSPARENT

			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
				// manufacturer-specific
			}
		}

		add {
			name = efSsec
			description =
				"The transparent EF.SSEC file shows the maximum SSEC values that have been defined for a specific application " +
				"environment of the HBA in accordance with the evaluation and confirmation of the HBA as a secure signature creation device."
			path = +"C005"
			shortEf = 0x05u
			type = DatasetType.TRANSPARENT

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
			resetAcl {
				paceProtectedAcl()
			}

			parameters {
				basePinParams()
				passwordRef = 0x01u
				pwdFlags =
					setOf(
						PasswordFlags.MODIFY_WITH_OLD_PASSWORD,
						PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK,
					)

				unblockingPassword {
					basePukParams()
				}
			}
		}
		signature {
			name = prk_hp_qes_r2048
			scope = DidScope.GLOBAL

			signAcl {
				pinProtectedPaceAcl("PIN.QES")
			}

			parameters {
				key {
					keyRef = 0x04u
					keySize = 2048
				}
				sigGen {
					standard {
						cardAlgRef = +"05"
					}
				}
				signatureAlgorithm = "SHA256withRSAandMGF1"
			}
		}

		signature {
			name = prk_hp_qes_e256
			scope = DidScope.GLOBAL

			signAcl {
				pinProtectedPaceAcl("PIN.QES")
			}

			parameters {
				key {
					keyRef = 0x06u
					keySize = 2048
				}

				sigGen {
					standard {
						cardAlgRef = +"00"
					}
				}
				signatureAlgorithm = "SHA256withECDSA"
			}
		}
	}
}

private fun ApplicationScope.appDFESIGN() {
	name = ESign.name
	aid = +"A000000167455349474E"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = ef_c_hp_aut_r2048
			description =
				"The file EF.C.HP.AUT.R2048 contains a certificate for cryptography with RSA with the public key PuK.HP.AUT.R2048."
			path = +"C500"
			shortEf = 0x01u
			type = DatasetType.TRANSPARENT

			readAcl {
				paceCmsCupProtectedAcl()
			}
			writeAcl {
				cmsCupProtectedAcl()
			}
		}

		add {
			name = ef_c_hp_enc_r2048
			description =
				"The file EF.C.HP.ENC.R2048 contains a certificate for cryptography with RSA with the public key PuK.HP.ENC.R2048."
			path = +"C200"
			shortEf = 0x02u
			type = DatasetType.TRANSPARENT

			readAcl {
				paceCmsCupProtectedAcl()
			}
			writeAcl {
				cmsCupProtectedAcl()
			}
		}

		add {
			name = ef_c_hp_aut_e256
			description =
				"The file EF.C.HP.AUT.E256 contains a certificate for cryptography with elliptic curves with the public key PuK.HP.AUT.E256."
			path = +"C506"
			shortEf = 0x06u
			type = DatasetType.TRANSPARENT

			readAcl {
				paceCmsCupProtectedAcl()
			}

			writeAcl {
				cmsCupProtectedAcl()
			}
		}

		add {
			name = ef_c_hp_enc_e256
			description =
				"The file EF.C.HP.ENC.E256 contains a certificate for cryptography with elliptic curves with the public key PuK.HP.ENC.E256."
			path = +"C205"
			shortEf = 0x05u
			type = DatasetType.TRANSPARENT

			readAcl {
				paceCmsCupProtectedAcl()
			}
			writeAcl {
				cmsCupProtectedAcl()
			}
		}

		add {
			name = ef_c_hp_sig_r2048
			description =
				"This EF contains the certificate for the key PrK.HP.SIG.R2048."
			path = +"C000"
			shortEf = 0x10u
			type = DatasetType.TRANSPARENT

			readAcl {
				paceCmsCupProtectedAcl()
			}
			writeAcl {
				cmsCupProtectedAcl()
			}
		}

		add {
			name = ef_c_hp_sig_e256
			description =
				"This EF contains the certificate for the key PrK.HP.SIG.E256."
			path = +"C007"
			shortEf = 0x07u
			type = DatasetType.TRANSPARENT

			readAcl {
				paceCmsCupProtectedAcl()
			}
			writeAcl {
				cmsCupProtectedAcl()
			}
		}
	}

	dids {
		signature {
			name = prk_hp_aut_r2048_signPKCS1_V1_5
			scope = DidScope.GLOBAL

			signAcl {
				pinChPaceProtectedAcl()
			}

			parameters {
				key {
					keyRef = 0x02u
					keySize = 2048
				}

				certificates("EF.C.HP.AUT.R2048")

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
			name = prk_hp_aut_r2048_signPSS
			scope = DidScope.GLOBAL

			signAcl {
				pinChPaceProtectedAcl()
			}

			parameters {
				key {
					keyRef = 0x02u
					keySize = 2048
				}

				certificates("EF.C.HP.AUT.R2048")
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
			name = prk_hp_enc_r2048
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
				certificates("EF.C.HP.ENC.R2048")
				cardAlgRef = +"85"
			}
		}

		signature {
			name = prk_hp_aut_e256
			scope = DidScope.GLOBAL

			signAcl {
				pinChPaceProtectedAcl()
			}

			parameters {
				key {
					keyRef = 0x06u
					keySize = 2048
				}

				certificates("EF.C.HP.AUT.E256")

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
			name = prk_hp_sig_r2048
			scope = DidScope.GLOBAL

			signAcl {
				pinChPaceProtectedAcl()
			}

			parameters {
				key {
					keyRef = 0x04u
					keySize = 2048
				}

				certificates("EF.C.HP.SIG.R2048")
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
			name = prk_hp_sig_e256
			scope = DidScope.GLOBAL

			signAcl {
				pinChPaceProtectedAcl()
			}

			parameters {
				key {
					keyRef = 0x07u
					keySize = 2048
				}

				certificates("EF.C.HP.SIG.E256")

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

private fun ApplicationScope.appDFCIAQES() {
	name = CiaQes.name
	aid = +"E828BD080FD27600006601"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = cia_qes_ef_cia_ciaInfo
			path = +"5032"
			shortEf = 0x12u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = cia_qes_efOd
			path = +"5031"
			shortEf = 0x11u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = cia_qes_efAod
			path = +"5034"
			shortEf = 0x14u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = cia_qes_efPrkd
			path = +"5035"
			shortEf = 0x15u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = cia_qes_efCd
			path = +"5038"
			shortEf = 0x16u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
	}
}

private fun ApplicationScope.appDFCIAESIGN() {
	name = CiaEsign.name
	aid = +"E828BD080FA000000167455349474E"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = cia_esign_ef_cia_ciaInfo
			path = +"5032"
			shortEf = 0x12u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = cia_esign_efOd
			path = +"5031"
			shortEf = 0x11u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = cia_esign_efAod
			path = +"5034"
			shortEf = 0x14u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = cia_esign_efPrkd
			path = +"5035"
			shortEf = 0x15u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
		add {
			name = cia_esign_efCd
			path = +"5038"
			shortEf = 0x16u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				neverAcl()
			}
		}
	}
}

private fun ApplicationScope.appDFAUTO() {
	name = Auto.name
	aid = +"D27600014603"

	selectAcl {
		alwaysAcl()
	}

	dataSets {
		add {
			name = ef_c_hp_auto1_r3072
			path = +"E001"
			shortEf = 0x01u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				pinProtectedPaceAcl(pinSo)
			}
		}

		add {
			name = ef_c_hp_auto2_r3072
			path = +"E002"
			shortEf = 0x02u
			type = DatasetType.TRANSPARENT
			readAcl {
				paceProtectedAcl()
			}
			writeAcl {
				pinProtectedPaceAcl(pinSo)
			}
		}
	}
	dids {
		pin {
			name = pinAuto
			scope = DidScope.GLOBAL
			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}
			resetAcl {
				paceProtectedAcl()
			}
			parameters {
				passwordRef = 0x01u
				pwdType = PasswordType.ISO_9564_1
				minLength = 5
				maxLength = 8
				storedLength = 8
				padChar = 0xFFu
				pwdFlags =
					setOf(
						PasswordFlags.MODIFY_WITH_OLD_PASSWORD,
						PasswordFlags.MODIFY_WITHOUT_OLD_PASSWORD, // manufacturer-specific with PACE
						PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK,
					)
				unblockingPassword {
					basePukParams()
				}
			}
		}

		pin {
			name = pinSo
			scope = DidScope.GLOBAL
			modifyAcl {
				paceProtectedAcl()
			}
			authAcl {
				paceProtectedAcl()
			}
			resetAcl {
				paceProtectedAcl()
			}
			parameters {
				basePinParams()
				passwordRef = 0x03u
				pwdFlags =
					setOf(
						PasswordFlags.MODIFY_WITH_OLD_PASSWORD,
						PasswordFlags.MODIFY_WITHOUT_OLD_PASSWORD, // manufacturer-specific with PACE
						PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK,
					)
				unblockingPassword {
					basePukParams()
				}
			}
		}

		signature {
			name = prk_hp_auto_r3072_signPKCS1_V1_5
			scope = DidScope.GLOBAL

			signAcl {
				pinProtectedPaceAcl(pinAuto)
			}

			parameters {
				key {
					keyRef = 0x02u
					keySize = 3072
				}

				signatureAlgorithm = "SHA256withRSA"

				sigGen {
					standard {
						cardAlgRef = +"02"
					}
				}
			}
		}
		signature {
			name = prk_hp_auto_r3072_signPSS
			scope = DidScope.GLOBAL

			signAcl {
				pinProtectedPaceAcl(pinAuto)
			}

			parameters {
				key {
					keyRef = 0x02u
					keySize = 3072
				}

				signatureAlgorithm = "SHA256withRSAandMGF1"
				sigGen {
					standard {
						cardAlgRef = +"05"
					}
				}
			}
		}
	}
}
