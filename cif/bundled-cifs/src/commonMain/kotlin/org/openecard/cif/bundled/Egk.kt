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
					// activeDidState("AUT_CMS")
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
					name = "EF.C.CA.CS.E256"
					description =
						"This file contains a CV certificate for cryptography with elliptical curves, which contains " +
						"the public key PuK.CA.CS.E256 of a CA. This certificate can be checked by means of the " +
						"public key PuK.RCA.CS.E256."
					path = +"2F07"
					shortEf = 0x07u
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
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
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
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

				pin {
					name = "MRPIN.home"
					scope = DidScope.GLOBAL

					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}

					parameters {
						pwdFlags = setOf(PasswordFlags.NEEDS_PADDING)
						pwdType = PasswordType.ISO_9564_1
						passwordRef = 0x02u
						minLength = 6
						maxLength = 8
						storedLength = 8
						padChar = 0xFFu
					}
				}
				pin {
					name = "MRPIN.NFD"
					scope = DidScope.GLOBAL

					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}

					parameters {
						passwordRef = 0x03u
						pwdFlags = setOf(PasswordFlags.NEEDS_PADDING)
						pwdType = PasswordType.ISO_9564_1
						minLength = 6
						maxLength = 8
						storedLength = 8
						padChar = 0xFFu
					}
				}
				pin {
					name = "MRPIN.DPE"
					scope = DidScope.GLOBAL

					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}

					parameters {
						pwdFlags = setOf(PasswordFlags.NEEDS_PADDING)
						pwdType = PasswordType.ISO_9564_1
						passwordRef = 0x04u
						minLength = 6
						maxLength = 8
						storedLength = 8
						padChar = 0xFFu
					}
				}
				pin {
					name = "MRPIN.GDD"
					scope = DidScope.GLOBAL

					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}

					parameters {
						pwdFlags = setOf(PasswordFlags.NEEDS_PADDING)
						pwdType = PasswordType.ISO_9564_1
						passwordRef = 0x05u
						minLength = 6
						maxLength = 8
						storedLength = 8
						padChar = 0xFFu
					}
				}
				pin {
					name = "MRPIN.NFD_READ"
					scope = DidScope.GLOBAL

					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}

					parameters {
						pwdFlags = setOf(PasswordFlags.NEEDS_PADDING)
						pwdType = PasswordType.ISO_9564_1
						passwordRef = 0x07u
						minLength = 6
						maxLength = 8
						storedLength = 8
						padChar = 0xFFu
					}
				}
				pin {
					name = "MRPIN.OSE"
					scope = DidScope.GLOBAL

					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}

					parameters {
						pwdFlags = setOf(PasswordFlags.NEEDS_PADDING)
						pwdType = PasswordType.ISO_9564_1
						passwordRef = 0x09u
						minLength = 6
						maxLength = 8
						storedLength = 8
						padChar = 0xFFu
					}
				}
				pin {
					name = "MRPIN.AMTS"
					scope = DidScope.GLOBAL

					modifyAcl {
						defaultModifyAcl()
					}
					authAcl {
						defaultAuthAcl()
					}

					parameters {
						pwdFlags = setOf(PasswordFlags.NEEDS_PADDING)
						pwdType = PasswordType.ISO_9564_1
						passwordRef = 0x0Cu
						minLength = 6
						maxLength = 8
						storedLength = 8
						padChar = 0xFFu
					}
				}
				pin {
					name = "MRPIN.AMTS_REP"
					scope = DidScope.GLOBAL

					modifyAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("PIN.CH")
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{ activeDidState("AUT_PACE") },
								{ activeDidState("PIN.CH") },
							)
						}
					}
					authAcl {
						defaultAuthAcl()
					}

					parameters {
						passwordRef = 0x0Du
						pwdType = PasswordType.ISO_9564_1
						minLength = 6
						maxLength = 8
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

		add {
			name = "DF.HCA"
			aid = +"D27600000102"
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
				}
			}
			val defaultWriteAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Grouped.CONTACT) {
					Always
				}
				acl(CardProtocol.Grouped.CONTACTLESS) {
					activeDidState("AUT_PACE")
				}
			}
			dataSets {
				add {
					name = "EF.Einwilligung"
					description =
						"This file contains information about the consents for voluntary applications."
					path = +"D005"
					shortEf = 0x05u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("MRPIN.home")
// 							PWD(MRPIN.home)
// 							OR   [PWD(PacePinId.PIN.CH)  AND   flagTI.25]
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("MRPIN.home")
								},
							)
// 							AUT_PACE
// 							AND   { PWD(MRPIN.home)
// 								OR     [PWD(PacePinId.PIN.CH)   AND   flagTI.25] }
						}
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Never
// 							CONTACT:
// 							PWD(PIN.CH)   AND   flagTI.27
// 							CONTACTLESS:
// 							AUT_PACE
// 							AND   [PWD(PIN.CH)   AND   flagTI.27]
						}
					}
				}

				add {
					name = "EF.GVD"
					description =
						"This file contains the protected insured person data. The details are described in Tab_eGK_ObjSys_035."
					path = +"D003"
					shortEf = 0x03u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("MRPIN.home")
// 							PWD(MRPIN.home)
// 							OR    [PWD(PIN.CH)   AND  flagTI.29]
// 							OR    flagTI.30
// 							OR    {AUT_VSD}
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("MRPIN.home")
									activeDidState("AUT_PACE")
								},
							)
// 							( AUT_PACE
// 								AND   { PWD(MRPIN.home)
// 								OR     [PWD(PacePinId.PIN.CH) AND flagTI.29]
// 								OR     flagTI.30 })
// 							OR      AUT_VSD
						}
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Never
							// AUT_VSD
						}
					}
				}

				add {
					name = "EF.Logging"
					description =
						"This file contains logging information about access to the eHC."
					path = +"D006"
					shortEf = 0x06u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							activeDidState("MRPIN.home")
// 							PWD(MRPIN.home)
// 							OR   [PWD(PIN.CH)   AND   flagTI.33]
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							and(
								{
									activeDidState("AUT_PACE")
									activeDidState("MRPIN.home")
								},
							)
// 							AUT_PACE
// 							AND   { PWD(MRPIN.home)
// 								OR   [ PWD(PIN.CH)   AND   flagTI.33] }
						}
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Never
						}
					}
				}

				add {
					name = "EF.PD"
					description =
						"This file contains the cardholder's personal data."
					path = +"D001"
					shortEf = 0x01u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Always
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							activeDidState("AUT_PACE")
// 							OR AUT_VSD
						}
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Never
// 							 AUT_VSD
						}
					}
				}

				add {
					name = "EF.Prüfungsnachweis"
					description =
						"This file stores a certificate that was created as part of an online check."
					path = +"D01C"
					shortEf = 0x1Cu
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}

				add {
					name = "EF.Standalone"
					description =
						"This file contains the information from EF.GVD and EF.DPE in encrypted form."
					path = +"DA0A"
					shortEf = 0x0Au
					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}

				add {
					name = "EF.StatusVD"
					description =
						"This file contains information about the status of the data in EF.PD, EF.VD and EF.GVD."
					path = +"D00C"
					shortEf = 0x0Cu

					readAcl {
						defaultReadAcl()
// 						CONTACTLESS:
// 							AUT_PACE
// 							OR     AUT_VSD
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Never
// 							 AUT_VSD
						}
					}
				}

				add {
					name = "EF.VD"
					description =
						"This file contains the insured person data."
					path = +"D002"
					shortEf = 0x02u
					readAcl {
						defaultReadAcl()
// 							CONTACTLESS:
// 							AUT_PACE
// 							OR    AUT_VSD
					}
					writeAcl {
						acl(CardProtocol.Any) {
							Never
// 							 AUT_VSD
						}
					}
				}

				add {
					name = "EF.Verweis"
					description =
						"This file contains information about the storage locations of the data of the voluntary applications that are not stored on the eHC."
					path = +"D009"
					shortEf = 0x09u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.24"),
// 									)
// 								},
							)
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.24"),
// 									)
// 								},
							)
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.28"),
// 									)
// 								},
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
// 								{
// 									and(
// 										activeDidState("AUT_PACE"),
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.28"),
// 									)
// 								},
							)
						}
					}
				}
			}
		}

		add {
			name = "DF.NFD"
			aid = +"D27600014407"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}
			dataSets {
				add {
					name = "EF.NFD"
					description =
						"This file contains an emergency data record."
					path = +"D010"
					shortEf = 0x10u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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

				add {
					name = "EF.StatusNFD"
					description =
						"This file contains information about the status of the emergency data record."
					path = +"D00E"
					shortEf = 0x0Eu
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
		}

		add {
			name = "DF.DPE"
			aid = +"D27600014408"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}
			dataSets {
				add {
					name = "EF.DPE"
					description =
						"This file contains the data record with the personal declarations of the insured person."
					path = +"D01B"
					shortEf = 0x1Bu

					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{ activeDidState("MRPIN.home") },
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 								{ activeDidState("flagTI.23") },
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
// 									activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 							)
						}
					}
				}

				add {
					name = "EF.StatusDPE"
					description =
						"This file contains information on the status of the data record with the personal declarations."
					path = +"D018"
					shortEf = 0x18u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{ activeDidState("MRPIN.home") },
// 								{
// 									and(
// 										activeDidState("PIN.CH"),
// 										activeDidState("flagTI.33"),
// 									)
// 								},
// 								{ activeDidState("flagTI.23") },
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
		}

		add {
			name = "DF.GDD"
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
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									activeDidState("AUT_PACE")
									activeDidState("MRPIN.home")
								},
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
							)
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									activeDidState("AUT_PACE")
									activeDidState("MRPIN.home")
								},
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
							)
						}
					}
				}

				add {
					name = "EF.VerweiseGDD"
					description =
						"This file contains information on the storage locations of the data of the voluntary health data services applications that are not stored on the eHC."
					path = +"D01A"
					shortEf = 0x1Au
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									activeDidState("AUT_PACE")
									activeDidState("MRPIN.home")
								},
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
							)
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									activeDidState("AUT_PACE")
									activeDidState("MRPIN.home")
								},
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
							)
						}
					}
				}
			}
		}

		add {
			name = "DF.OSE"
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
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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

				add {
					name = "EF.StatusOSE"
					description =
						"This file contains information on the status of the organ donation declaration."
					path = +"E002"
					shortEf = 0x02u

					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
		}

		add {
			name = "DF.AMTS"
			aid = +"D2760001440C"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}

			dataSets {
				add {
					name = "EF.AMTS"
					description =
						"This file contains a data set for AMTS data management."
					path = +"E005"
					shortEf = 0x05u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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

				add {
					name = "EF.VerweiseAMTS"
					description =
						"This file contains information on the storage locations of data from the voluntary AMTS data management application that is not stored on the eHC."
					path = +"E006"
					shortEf = 0x06u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
				}

				add {
					name = "EF.StatusAMTS"
					description =
						"This file contains information on the status of the AMTS Data Management application."
					path = +"E007"
					shortEf = 0x07u
					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
					}
					writeAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							Never
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
						}

						acl(CardProtocol.Grouped.CONTACTLESS) {
							Never
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
				}
			}
			val defaultWriteAcl: (AclScope.() -> Unit) = {
				acl(CardProtocol.Any) {
					Never
				}
			}

			dataSets {
				add {
					name = "EF.C.CH.AUT.R2048"
					description =
						"This file contains a certificate for cryptography with RSA with the public key PuK.CH.AUT.R2048 to PrK.CH.AUT.R2048."
					path = +"C500"
					shortEf = 0x01u

					readAcl {
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
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
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}
					writeAcl {
						defaultWriteAcl()
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
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = " EF.C.CH.ENCV.R2048"
					description =
						"This file contains a certificate for cryptography with RSA with the public key PuK.CH.ENCV.R2048 to PrK.CH.ENCV.R2048."
					path = +"C50A"
					shortEf = 0x0Au

					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}
					writeAcl {
						defaultWriteAcl()
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
						defaultReadAcl()
// 						CONTACTLESS:
// 						OR AUT_CMS
					}

					writeAcl {
						defaultWriteAcl()
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
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}

					writeAcl {
						defaultWriteAcl()
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
						defaultReadAcl()
					}
					writeAcl {
						defaultWriteAcl()
					}
				}
				add {
					name = "EF.C.CH.ENCV.E256"
					description =
						"This file contains an encryption certificate for elliptic curve cryptography with the public key PuK.CH.ENCV.E256 to PrK.CH.ENCV.E256."
					path = +"C50C"
					shortEf = 0x0Cu

					readAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
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
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}
					writeAcl {
						defaultWriteAcl()
// 						AUT_CMS
					}
				}
			}

			dids {

				signature {
					name = "PrK.CH.AUT.R2048_signPKCS1_V1_5"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							activeDidState("AUT_PACE")
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
							)
						}
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
					name = "PrK.CH.AUT.R2048_signPSS"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							activeDidState("AUT_PACE")
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
							)
						}
					}

					parameters {

						key {
							keyRef = 0x02u
							keySize = 2048
						}

						certificates("EF.C.CH.AUT.R2048")
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
					name = "PrK.CH.AUTN.R2048_signPSS"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.8")
// 								)
// 							},
// 								{
// 									activeDidState("flagTI.9")
// 								},
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							activeDidState("AUT_PACE")
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}

					parameters {
						key {
							keyRef = 0x06u
							keySize = 2048
						}
						certificates("EF.C.CH.AUTN.R2048")
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
					name = "PrK.CH.ENC_rsaDecipherOaep"
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
						certificates("EF.C.CH.ENC.R2048")
						cardAlgRef = +"85"
					}
				}

				encrypt {
					name = "PrK.CH.ENC_rsaDecipherPKCS1_V1_5"
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

						encryptionAlgorithm = "RSAWithSHA-256AndMGF1Padding"
						certificates("EF.C.CH.ENC.R2048")
						cardAlgRef = +"81" // rsaDecipherOaep 1000 01012 = '85' ?
					}
				}

				encrypt {
					name = "PrK.CH.ENCV_rsaDecipherOaep"
					scope = DidScope.LOCAL

					encipherAcl {
						acl(CardProtocol.Any) {
							Never
						}
					}

					parameters {

						key {
							keyRef = 0x07u
							keySize = 2048
						}

						encryptionAlgorithm = "OAEPWithSHA-256AndMGF1Padding"
						certificates("EF.C.CH.ENCV.R2048")
						cardAlgRef = +"85"
					}
				}

				encrypt {
					name = "PrK.CH.ENCV_rsaDecipherPKCS1_V1_5"
					scope = DidScope.LOCAL

					encipherAcl {
						acl(CardProtocol.Any) {
							Never
						}
					}

					parameters {

						key {
							keyRef = 0x07u
							keySize = 2048
						}

						encryptionAlgorithm = "RSAWithSHA-256AndMGF1Padding"
						certificates("EF.C.CH.ENCV.R2048")
						cardAlgRef = +"81"
					}
				}

				signature {
					name = "PrK.CH.AUT.E256"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
// 							{
// 								and(
// 									activeDidState("AUT_PACE"),
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.12")
// 								)
// 							}
							)
						}
					}

					parameters {
						key {
							keyRef = 0x04u
							keySize = 2048
						}

						certificates("EF.C.CH.AUT.E256")

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
					name = "PrK.CH.AUTN.E256"
					scope = DidScope.GLOBAL

					signAcl {
						acl(CardProtocol.Grouped.CONTACT) {
							or(
								{
									activeDidState("MRPIN.home")
								},
// 							{
// 								and(
// 									activeDidState("PIN.CH"),
// 									activeDidState("flagTI.8")
// 								)
// 							},
// 								{
// 									activeDidState("flagTI.9")
// 								},
							)
						}
						acl(CardProtocol.Grouped.CONTACTLESS) {
							activeDidState("AUT_PACE")
							or(
								{
									and(
										activeDidState("AUT_PACE"),
										activeDidState("MRPIN.home"),
									)
								},
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
							)
						}
					}

					parameters {
						key {
							keyRef = 0x0Bu
							keySize = 2048
						}
						certificates("EF.C.CH.AUTN.E256")
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

			dataSets {
				add {
					name = "EF.C.CH.QES.R2048"
					description =
						"This file contains a certificate for cryptography with RSA with the public key PuK.CH.QES.R2048 to PrK.CH.QES.R2048."
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
					name = "EF.C.CH.QES.E256"
					description =
						"This file contains a certificate for cryptography with elliptic curves with the public key PuK.CH.QES.E256 to PrK.CH.QES.E256."
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
			}

			dids {
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

				pin {
					name = "PIN.QES"
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
						minLength = 6
						maxLength = 8
					}
				}
				signature {
					name = "PrK.CH.QES.R2048"
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
					name = "PrK.CH.QES.E256"
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
	}
	b.build()
}
