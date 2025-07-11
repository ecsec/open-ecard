package org.openecard.cif.bundled

import kotlinx.datetime.Instant
import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.definition.did.PacePinId
import org.openecard.cif.definition.did.PasswordFlags
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.definition.did.SignatureGenerationInfoType
import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.api.acl.AclBoolTreeBuilder.activeDidState
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.builder.CardInfoBuilder
import org.openecard.cif.dsl.builder.unaryPlus

@OptIn(ExperimentalUnsignedTypes::class)
val HbaCif by lazy {
	val b = CardInfoBuilder()

	b.metadata {
		id = "http://www.baek.de/cif/HPC-G2"
		version = "1.0.0"
		status = CardInfoStatus.DEVELOPMENT
		name = "HPC-G2"
		cardIssuer = "Bundesaerztekammer"
		creationDate = Instant.parse("2025-06-25T00:00:00Z")
		modificationDate = Instant.parse("2025-06-25T00:00:00Z")
	}

	b.applications {

		add {
			name = "MF"
			aid = +"D2760001448000"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}

			// add datasets
			val defaultReadAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
					// or(
					// 	{ activeDidState("AUT_PACE") },
					// 	 { activeDidState("AUT_CMS") },
					// )
				}
			}
			val defaultWriteAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Any) {
					Never
					// activeDidState("AUT_CMS")
				}
			}
			val defaultModifyAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}
			val defaultAuthAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
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
						acl(CardProtocol.Any) {
							Always
						}
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Always
						}
					}
				}

				add {
					name = "EF.CardAccess"
					description =
						"EF.CardAccess is required for the PACE protocol when using the contactless interface."
					path = +"011C"
					shortEf = 0x1Cu
					readAcl {
						acl(CardProtocol.Any) {
							Always
						}
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Never
						}
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
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
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
						acl(CardProtocol.Grouped.CONTACT) {
							Always
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							activeDidState("AUT_PACE")
						}
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Never
						}
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
						acl(CardProtocol.Any) {
							Always
						}
					}

					writeAcl {
						acl(CardProtocol.Any) {
							// activeDidState("AUT_CMS")
							Never
						}
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
						defaultReadAcl()
// 						AUT_CMS   OR   AUT_CUP   OR   AUT_PACE
					}
					writeAcl {
						defaultWriteAcl()
// 						AUT_CMS   OR   AUT_CUP
					}
				}

				add {
					name = "EF.C.HPC.AUTR_CVC.E256"
					description =
						"EF.C.HPC.AUTR_CVC.E256 contains the CV certificate of the HBA for cryptography with elliptic curves " +
						"for role-based C2C authentication between HBA and eGK and for the authorization of the SMC-B. " +
						"This certificate can be checked using the public key from EF.C.CA.CS.E256 (see Tab_HBA_ObjSys_011)."
					path = +"2F06"
					shortEf = 0x06u
					readAcl {
						defaultReadAcl()
// 						AUT_CMS  OR AUT_CUP OR AUT_PACE
					}
					writeAcl {
						defaultWriteAcl()
// 						AUT_CMS OR AUT_CUP
					}
				}

				add {
					name = "EF.C.HPC.AUTD_SUK_CVC.E256"
					description =
						"EF.C.HPC.AUTD_SUK_CVC.E256 contains the CV certificate of the HBA for cryptography with elliptic curves for " +
						"function-based C2C authentication between HBA/gSMC-KT and HBA/gSMC-K with the HBA as a signature card " +
						"for batch and convenience signatures (SUK) in order to receive PIN data and the data to be signed (DTBS)."
					path = +"2F09"
					shortEf = 0x09u
					readAcl {
						defaultReadAcl()
// 						AUT_CMS  OR AUT_CUP OR AUT_PACE
					}
					writeAcl {
						defaultWriteAcl()
// 						AUT_CMS OR AUT_CUP
					}
				}
			}

			dids {
				pace {
					name = "AUT_PACE"
					scope = DidScope.GLOBAL
					modifyAcl {
						acl(CardProtocol.Any) {
							Never
						}
					}

					authAcl {
						acl(CardProtocol.Any) {
							Always
						}
					}
					parameters {
						passwordRef = PacePinId.CAN
						minLength = 6
						maxLength = 8
					}
				}

				pin {
					name = "PIN.CH"
					scope = DidScope.GLOBAL
					modifyAcl {
						// didUpdate
						defaultModifyAcl()
					}
					authAcl {
						// DIDAuthenticate
						defaultAuthAcl()
					}
					parameters {
						pwdFlags = setOf(PasswordFlags.NEEDS_PADDING)
						pwdType = PasswordType.ISO_9564_1
						passwordRef = 0x01u
						minLength = 6
						maxLength = 8
						storedLength = 8
						padChar = 0xFFu
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

		add {
			name = "DF.HPA"
			aid = +"D27600014602"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}

			dataSets {
				add {
					name = "EF.HPD"
					description =
						"The transparent EF.HPD file is intended for storing data relating to the respective healthcare professional, " +
						"e.g. confirmation of participation in further training measures. The file can always be read, but an update " +
						"is only possible after the PIN.CH has been successfully entered."
					path = +"D001"
					shortEf = 0x01u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Always
						}

						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("AUT_PACE")
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.CH")
						}

						acl(CardProtocol.Grouped.CONTACT) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.CH")
								},
							)
						}
					}
				}
			}
		}

		add {
			name = "DF.QES"
			aid = +"D27600006601"
			description = "Optional QES application"

			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}
			val defaultReadAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}
			val defaultWriteAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Any) {
					Never
				}
			}

			val defaultModifyAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}
			val defaultAuthAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}

			dataSets {
				add {
					name = "EF.C.HP.QES.R2048"
					description =
						"The transparent file EF.C.HP.QES.R2048 contains the X.509 certificate for cryptography with RSA with the " +
						"public key of the healthcare professional PuK.HP.QES.R2048 for the qualified electronic signature in " +
						"accordance with EU Regulation No. 910/2014 (eIDAS)."
					path = +"C000"
					shortEf = 0x10u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
						// manufacturer-specific
					}
				}

				add {
					name = "EF.C.HP.QES.E256"
					description =
						"The transparent file EF.C.HP.QES.E256 contains the X.509 certificate for cryptography with elliptic curves " +
						"with the public key of the healthcare professional PuK.HP.QES.E256 for the qualified electronic signature " +
						"in accordance with EU Regulation No. 910/2014 (eIDAS)."
					path = +"C006"
					shortEf = 0x06u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
						// manufacturer-specific
					}
				}

				add {
					name = "EF.SSEC"
					description =
						"The transparent EF.SSEC file shows the maximum SSEC values that have been defined for a specific application " +
						"environment of the HBA in accordance with the evaluation and confirmation of the HBA as a secure signature creation device."
					path = +"C005"
					shortEf = 0x05u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
						// manufacturer-specific
					}
				}
			}

			dids {

				pin {
					name = "PIN.QES"
					scope = DidScope.GLOBAL

					modifyAcl {
						defaultAuthAcl()
					}

					authAcl {
						defaultAuthAcl()
					}

					parameters {
						passwordRef = 0x01u
						pwdType = PasswordType.ISO_9564_1
						minLength = 6
						maxLength = 8
					}
				}
				signature {
					name = "PrK.HP.QES.R2048"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.QES")
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.QES")
								},
							)
						}
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
						signatureAlgorithm = "SHA256withRSASSA-PSSandMGF1"
					}
				}

				signature {
					name = "PrK.HP.QES.E256"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.QES")
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.QES")
								},
							)
						}
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
						signatureAlgorithm = "SHA256withECDSAandMGF1"
					}
				}
			}
		}

		add {
			name = "DF.ESIGN"
			aid = +"A000000167455349474E"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}
			val defaultReadAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
// 					AUT_CMS OR AUT_CUP OR AUT_PACE
				}
			}
			val defaultWriteAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Any) {
					Never
// 						AUT_CMS OR AUT_CUP
				}
			}

			dataSets {
				add {
					name = "EF.C.HP.AUT.R2048"
					description =
						"The file EF.C.HP.AUT.R2048 contains a certificate for cryptography with RSA with the public key PuK.HP.AUT.R2048."
					path = +"C500"
					shortEf = 0x01u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}

				add {
					name = "EF.C.HP.ENC.R2048"
					description =
						"The file EF.C.HP.ENC.R2048 contains a certificate for cryptography with RSA with the public key PuK.HP.ENC.R2048."
					path = +"C200"
					shortEf = 0x02u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}

				add {
					name = "EF.C.HP.AUT.E256"
					description =
						"The file EF.C.HP.AUT.E256 contains a certificate for cryptography with elliptic curves with the public key PuK.HP.AUT.E256."
					path = +"C506"
					shortEf = 0x06u

					readAcl {
						defaultReadAcl()
					}

					writeAcl {
						defaultWriteAcl()
					}
				}

				add {
					name = "EF.C.HP.ENC.E256"
					description =
						"The file EF.C.HP.ENC.E256 contains a certificate for cryptography with elliptic curves with the public key PuK.HP.ENC.E256."
					path = +"C205"
					shortEf = 0x05u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}

				add {
					name = "EF.C.HP.SIG.R2048"
					description =
						"This EF contains the certificate for the key PrK.HP.SIG.R2048."
					path = +"C000"
					shortEf = 0x10u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}

				add {
					name = "EF.C.HP.SIG.E256"
					description =
						"This EF contains the certificate for the key PrK.HP.SIG.E256."
					path = +"C007"
					shortEf = 0x07u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
			}

			dids {

				signature {
					name = "PrK.HP.AUT.R2048_signPKCS1_V1_5"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.CH")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.CH")
								},
							)
						}
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
					name = "PrK.HP.AUT.R2048_signPSS"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.CH")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.CH")
								},
							)
						}
					}

					parameters {

						key {
							keyRef = 0x02u
							keySize = 2048
						}

						certificates("EF.C.HP.AUT.R2048")
						signatureAlgorithm = "SHA256withRSASSA-PSSandMGF1"
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
					name = "PrK.HP.ENC_rsaDecipherOaep"
					scope = DidScope.LOCAL

					encipherAcl {
						acl(CardProtocol.Any) {
							Never
						}
					}

					parameters {

						key {
							keyRef = 0x03u
							keySize = 2048
						}

						encryptionAlgorithm = "OAEPWithSHA-256AndMGF1Padding"
						certificates("EF.C.HP.ENC.R2048")
						cardAlgRef = +"85"
					}
				}

				signature {
					name = "PrK.HP.AUT.E256"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.CH")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.CH")
								},
							)
						}
					}

					parameters {
						key {
							keyRef = 0x06u
							keySize = 2048
						}

						certificates("EF.C.HP.AUT.E256")

						signatureAlgorithm = "SHA256withECDSAandMGF1"

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
					name = "PrK.HP.SIG.R2048"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.CH")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.CH")
								},
							)
						}
					}

					parameters {

						key {
							keyRef = 0x04u
							keySize = 2048
						}

						certificates("EF.C.HP.SIG.R2048")
						signatureAlgorithm = "SHA256withRSASSA-PSSandMGF1"
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
					name = "PrK.HP.SIG.E256"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.CH")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.CH")
								},
							)
						}
					}

					parameters {
						key {
							keyRef = 0x07u
							keySize = 2048
						}

						certificates("EF.C.HP.SIG.E256")

						signatureAlgorithm = "SHA256withECDSAandMGF1"

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

		add {
			name = "DF.CIA.QES"
			aid = +"E828BD080FD27600006601"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}

			val defaultReadAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}
			val defaultWriteAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Any) {
					Never
				}
			}

			dataSets {
				add {
					name = "DF.CIA.QES/EF.CIA.CIAInfo"
					path = +"5032"
					shortEf = 0x12u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "DF.CIA.QES/EF.OD"
					path = +"5031"
					shortEf = 0x11u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "DF.CIA.QES/EF.AOD"
					path = +"5034"
					shortEf = 0x14u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "DF.CIA.QES/EF.PrKD"
					path = +"5035"
					shortEf = 0x15u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "DF.CIA.QES/EF.CD"
					path = +"5038"
					shortEf = 0x16u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
			}
		}
		add {
			name = "DF.CIA.ESIGN"
			aid = +"E828BD080FA000000167455349474E"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}

			val defaultReadAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}
			val defaultWriteAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Any) {
					Never
				}
			}

			dataSets {
				add {
					name = "DF.CIA.ESIGN/EF.CIA.CIAInfo"
					path = +"5032"
					shortEf = 0x12u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "DF.CIA.ESIGN/EF.OD"
					path = +"5031"
					shortEf = 0x11u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "DF.CIA.ESIGN/EF.AOD"
					path = +"5034"
					shortEf = 0x14u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "DF.CIA.ESIGN/EF.PrKD"
					path = +"5035"
					shortEf = 0x15u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "DF.CIA.ESIGN/EF.CD"
					path = +"5038"
					shortEf = 0x16u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
			}
		}
		add {
			name = "DF.AUTO"
			aid = +"D27600014603"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}

			val defaultReadAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}

			val defaultModifyAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}
			val defaultAuthAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}

			dataSets {
				add {
					name = "EF.C.HP.AUTO1.R3072"
					path = +"E001"
					shortEf = 0x01u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.SO")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.SO")
								},
							)
						}
					}
				}

				add {
					name = "EF.C.HP.AUTO2.R3072"
					path = +"E002"
					shortEf = 0x02u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.SO")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.SO")
								},
							)
						}
					}
				}
			}
			dids {
				pin {
					name = "PIN.AUTO"
					scope = DidScope.GLOBAL
					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}
					parameters {
						passwordRef = 0x01u
						pwdType = PasswordType.ISO_9564_1
						minLength = 5
						maxLength = 8
					}
				}

				pin {
					name = "PIN.SO"
					scope = DidScope.GLOBAL
					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}
					parameters {
						passwordRef = 0x03u
						pwdType = PasswordType.ISO_9564_1
						minLength = 6
						maxLength = 8
					}
				}

				signature {
					name = "PrK.HP.AUTO.R3072_signPKCS1_V1_5"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.AUTO")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.AUTO")
								},
							)
						}
					}

					parameters {
						key {
							keyRef = 0x02u
							keySize = 3072
						}

						signatureAlgorithm = "SHA256withRSAandMGF1"

						sigGen {
							standard {
								cardAlgRef = +"02"
							}
						}
					}
				}
				signature {
					name = "PrK.HP.AUTO.R3072_signPSS"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.AUTO")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("PIN.AUTO")
								},
							)
						}
					}

					parameters {

						key {
							keyRef = 0x02u
							keySize = 3072
						}

						signatureAlgorithm = "SHA256withRSASSA-PSSandMGF1"
						sigGen {
							standard {
								cardAlgRef = +"05"
							}
						}
					}
				}
			}
		}
	}
	b.build()
}
