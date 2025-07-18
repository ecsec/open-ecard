package org.openecard.cif.bundled

import kotlinx.datetime.Instant
import org.openecard.cif.bundled.GematikBuildingBlocks.alwaysAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.neverAcl
import org.openecard.cif.bundled.NpaDefinitions.EId
import org.openecard.cif.bundled.NpaDefinitions.ESign
import org.openecard.cif.bundled.NpaDefinitions.Mf
import org.openecard.cif.bundled.NpaDefinitions.canPinTaKeyProtectedAcl
import org.openecard.cif.bundled.NpaDefinitions.canTaKeyCaKeyCiProtectedAcl
import org.openecard.cif.bundled.NpaDefinitions.datasetDG
import org.openecard.cif.bundled.NpaDefinitions.pinCanTaKeyCaKeyCiProtectedAcl
import org.openecard.cif.bundled.NpaDefinitions.pinTaKeyCaKeyCiProtectedAcl
import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.acl.PaceAclQualifier
import org.openecard.cif.definition.capabilities.CommandCodingDefinitions
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.definition.did.PacePinId
import org.openecard.cif.definition.did.PasswordFlags
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.definition.did.SignatureGenerationInfoType
import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.application.ApplicationScope
import org.openecard.cif.dsl.api.dataset.DataSetScope
import org.openecard.cif.dsl.builder.CardInfoBuilder
import org.openecard.cif.dsl.builder.unaryPlus
import org.openecard.utils.serialization.PrintableUByteArray

@OptIn(ExperimentalUnsignedTypes::class)
val NpaCif by lazy {
	val b = CardInfoBuilder()

	b.metadata {
		id = "http://bsi.bund.de/cif/npa.xml"
		version = "1.0.0"
		status = CardInfoStatus.DEVELOPMENT
		name = "German Electronic Identity Card"
		cardIssuer = "Federal Office for Information Security"
		creationDate = Instant.parse("2025-06-25T00:00:00Z")
		modificationDate = Instant.parse("2025-06-25T00:00:00Z")
	}

	b.capabilities {
		selectionMethods {
			selectDfByFullName = true
			selectDfByPartialName = false
			selectDfByPath = false
			selectDfByFileId = false
			selectDfImplicit = false
			supportsShortEf = true
			supportsRecordNumber = false
			supportsRecordIdentifier = false
		}

		dataCoding {
			tlvEfs = false
			writeOneTime = true
			writeProprietary = false
			writeOr = false
			writeAnd = false
			ffValidAsTlvFirstByte = false
			dataUnitsQuartets = 2
		}

		commandCoding {
			supportsCommandChaining = true
			supportsExtendedLength = true
			logicalChannel = CommandCodingDefinitions.LogicalChannelAssignment.NO_LOGICAL_CHANNELS
			maximumLogicalChannels = 0
		}
	}

	b.applications {
		add {
			appMf()
		}

		add {
			appeId()
		}

		add {
			appeSign()
		}
	}
	b.build()
}

object NpaDefinitions {
	object Mf {
		val name = "MF"

		object Datasets {
			val efDir = "EF.DIR"
			val efAtr = "EF.ATR"
			val efCardAccess = "EF.CardAccess"
			val efCardSecurity = "EF.CardSecurity"
			val efChipSecurity = "EF.ChipSecurity"
		}

		object Dids {
			val pin = "PIN"
			val pacePin = "PACE_PIN"
			val paceCan = "PACE_CAN"
			val pacePuk = "PACE_PUK"
		}
	}

	object EId {
		val name = "eID"

		object Datasets {
			val DG1 = "DG1"
			val DG2 = "DG2"
			val DG3 = "DG3"
			val DG4 = "DG4"
			val DG5 = "DG5"
			val DG6 = "DG6"
			val DG7 = "DG7"
			val DG8 = "DG8"
			val DG9 = "DG9"
			val DG10 = "DG10"
			val DG11 = "DG11"
			val DG17 = "DG17"
		}
	}

	object ESign {
		val name = "eSign"

		object Datasets {
			val ef_c_zda_qes = "EF.C.ZDA.QES"
			val ef_c_icc_qes = "EF.C.ICC.QES"
		}

		object Dids {
			val prk_icc_qes = "PrK.ICC.QES"
			val esignPin = "eSign_PIN"
		}
	}

	internal fun AclScope.pinTaKeyCaKeyCiProtectedAcl(
		pin: String,
		taKey: PrintableUByteArray,
	) {
		neverAcl()
// 		acl(CardProtocol.Any) {
// 			or(
// 				{
// 					and(
// 						activeDidState(pin),
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(taKey),
// 						),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 				{
// 					and(
// 						activeDidState(pin),
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(taKey),
// 						),
// 						activeDidState("CAKey-ci"),
// 					)
// 				},
// 			)
// 		}
	}

	internal fun AclScope.pinCanTaKeyCaKeyCiProtectedAcl() {
		neverAcl()
// 		acl(CardProtocol.Any) {
// 			or(
// 				{
// 					and(
// 						activeDidState("CAN"),
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(+"7F4C12060904007F000703010203530102"),
// 						),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 				{
// 					and(
// 						activeDidState("CAN"),
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(+"7F4C12060904007F000703010203530102"),
// 						),
// 						activeDidState("CAKey-ci"),
// 					)
// 				},
// 				{
// 					and(
// 						activeDidState("PIN"),
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(+"7F4C12060904007F000703010203530102"),
// 						),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 				{
// 					and(
// 						activeDidState("PIN"),
// 						activeDidState(
// 							"TAKey",
// 		PaceAclQualifier(+"7F4C12060904007F000703010203530102"),
// 						),
// 						activeDidState("CAKey-ci"),
// 					)
// 				},
// 			)
// 		}
	}

	internal fun AclScope.canTaKeyCaKeyCiProtectedAcl() {
		neverAcl()
// 		acl(CardProtocol.Any) {
// 			or(
// 				{
// 					and(
// 						activeDidState("CAN"),
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(+"7F4C12060904007F000703010203530102"),
// 						),
// 						activeDidState("CAKey"),
// 						activeDidState("eSign-PIN"),
// 					)
// 				},
// 				{
// 					and(
// 						activeDidState("CAN"),
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(+"7F4C12060904007F000703010203530102"),
// 						),
// 						activeDidState("CAKey-ci"),
// 						activeDidState("eSign-PIN"),
// 					)
// 				},
// 			)
// 		}
	}

	internal fun AclScope.canPinTaKeyProtectedAcl(
		taKey1: PaceAclQualifier?,
		taKey2: PaceAclQualifier?,
	) {
		neverAcl()
// 				acl(CardProtocol.Any) {
// 					or(
// 						{
// 							and(
// 								activeDidState("CAN"),
// 								activeDidState(
// 									"TAKey",
// 									taKey1,
// 								),
// 							)
// 						},
// 						{
// 							and(
// 								activeDidState("PIN"),
// 								activeDidState(
// 									"TAKey",
// 									taKey2,
// 								),
// 							)
// 						},
// 					)
// 				}
	}

	internal fun DataSetScope.datasetDG(
		name: String,
		path: PrintableUByteArray,
		taKey2: PrintableUByteArray,
	) {
		this.name = name
		this.path = path

		readAcl {
			neverAcl()
// 			acl(CardProtocol.Any) {
// 				or(
// 					{
// 						and(
// 							activeDidState("PIN"),
// 							activeDidState(
// 								"TAKey",
// 								PaceAclQualifier(+"7F4C12060904007F00070301020253050000000010"),
// 							),
// 							activeDidState(
// 								"TAKey",
// 								PaceAclQualifier(taKey2),
// 							),
// 							activeDidState("CAKey"),
// 						)
// 					},
// 					{
// 						and(
// 							activeDidState("CAN"),
// 							activeDidState(
// 								"TAKey",
// 								PaceAclQualifier(+"7F4C12060904007F00070301020253050000000010"),
// 							),
// 							activeDidState(
// 								"TAKey",
// 								PaceAclQualifier(taKey2),
// 							),
// 							activeDidState("CAKey"),
// 						)
// 					},
// 				)
// 			}
		}

		writeAcl {
			neverAcl()
		}
	}
}

private fun ApplicationScope.appMf() {
	name = Mf.name
	aid = +"3F00"
	selectAcl {
		acl(CardProtocol.Grouped.CONTACTLESS) {
			Always
		}
	}

	dataSets {
		add {
			name = Mf.Datasets.efDir
			description =
				"The EF.DIR file contains a list of application templates according to ISO7816-4. This list " +
				"is adjusted when the application structure changes by deleting or creating applications."
			path = +"2F00"
			readAcl {
				alwaysAcl()
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = Mf.Datasets.efAtr
			description =
				"The transparent file EF.ATR contains information about the maximum size of the APDU. " +
				"It is also used to version variable elements of a map."
			path = +"2F01"
			readAcl {
				alwaysAcl()
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = Mf.Datasets.efCardAccess
			description =
				"EF.CardAccess is required for the PACE protocol when using the contactless interface."
			path = +"011C"
			readAcl {
				alwaysAcl()
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = Mf.Datasets.efCardSecurity
			path = +"011D"
			readAcl {
				canPinTaKeyProtectedAcl(null, null)
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = Mf.Datasets.efChipSecurity
			path = +"011B"
			readAcl {
				canPinTaKeyProtectedAcl(
					PaceAclQualifier(+"7F4C12060904007F00070301020253050000000018"),
					PaceAclQualifier(+"7F4C12060904007F00070301020253050000000008"),
				)
			}
			writeAcl {
				neverAcl()
			}
		}
	}
	dids {
		pin {
			name = Mf.Dids.pin
			scope = DidScope.GLOBAL

			authAcl {
				acl(CardProtocol.Any) {
					or(
						{ activeDidState(Mf.Dids.pacePin) },
						{ activeDidState(Mf.Dids.pacePuk) },
					)
				}
			}

			resetAcl {
				acl(CardProtocol.Any) {
					activeDidState(Mf.Dids.pin)
				}
			}

			parameters {
				passwordRef = 0x03u
				pwdType = PasswordType.ASCII_NUMERIC
				pwdFlags =
					setOf(
						PasswordFlags.RESET_RETRY_COUNTER_WITH_PASSWORD,
						PasswordFlags.RESET_RETRY_COUNTER_WITHOUT_DATA,
					)
				minLength = 6
				maxLength = 6
				storedLength = 6
			}
		}

		pace {
			name = Mf.Dids.pacePin
			scope = DidScope.GLOBAL

			authAcl {
				alwaysAcl()
			}

			parameters {
				passwordRef = PacePinId.PIN
				minLength = 5
				maxLength = 6
			}
		}

		pace {
			name = Mf.Dids.paceCan
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

		pace {
			name = Mf.Dids.pacePuk
			scope = DidScope.GLOBAL

			authAcl {
				alwaysAcl()
			}

			parameters {
				passwordRef = PacePinId.PUK
				minLength = 10
				maxLength = 10
			}
		}
// 		EAC
// 		TAKey
// 		CAKey
// 		CAKey-ci
// 		RIKey
// 		RIKEY-aa
	}
}

private fun ApplicationScope.appeId() {
	name = EId.name
	aid = +"E80704007F00070302"
	selectAcl {
		acl(CardProtocol.Grouped.CONTACTLESS) {
			Never
// 			or(
// 				{
// 					and(
// 						activeDidState("PIN"),
// 						activeDidState("TAKey"),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 				{
// 					and(
// 						activeDidState("CAN"),
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(+"7F4C12060904007F00070301020253050000000010"),
// 						),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 			)
		}
	}

	dataSets {
		add {
			datasetDG(
				name = EId.Datasets.DG1,
				path = +"0101",
				+"7F4C12060904007F00070301020253050000000100",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG2,
				path = +"0102",
				+"7F4C12060904007F00070301020253050000000200",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG3,
				path = +"0103",
				+"7F4C12060904007F00070301020253050000004000",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG4,
				path = +"0104",
				+"7F4C12060904007F00070301020253050000000800",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG5,
				path = +"0105",
				+"7F4C12060904007F00070301020253050000001000",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG6,
				path = +"0106",
				+"7F4C12060904007F00070301020253050000002000",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG7,
				path = +"0107",
				+"7F4C12060904007F00070301020253050000004000",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG8,
				path = +"0108",
				+"7F4C12060904007F00070301020253050000008000",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG9,
				path = +"0109",
				+"7F4C12060904007F00070301020253050000010000",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG10,
				path = +"010A",
				+"7F4C12060904007F00070301020253050000020000",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG11,
				path = +"010B",
				+"7F4C12060904007F00070301020253050000040000",
			)
		}

		add {
			datasetDG(
				name = EId.Datasets.DG17,
				path = +"0111",
				+"7F4C12060904007F00070301020253050001000000",
			)

// 			writeAcl {
// 				acl(CardProtocol.Any) {
// 					or(
// 						{
// 							and(
// 								activeDidState("PIN"),
// 								activeDidState(
// 									"TAKey",
// 									PaceAclQualifier(+"7F4C12060904007F00070301020253050000000010"),
// 								),
// 								activeDidState(
// 									"TAKey",
// 									PaceAclQualifier(+"7F4C12060904007F00070301020253052000000000"),
// 								),
// 								activeDidState("CAKey"),
// 							)
// 						},
// 						{
// 							and(
// 								activeDidState("CAN"),
// 								activeDidState(
// 									"TAKey",
// 									PaceAclQualifier(+"7F4C12060904007F00070301020253050000000010"),
// 								),
// 								activeDidState(
// 									"TAKey",
// 									PaceAclQualifier(+"7F4C12060904007F00070301020253052000000000"),
// 								),
// 								activeDidState("CAKey"),
// 							)
// 						},
// 					)
// 				}
// 			}
		}
	}
}

private fun ApplicationScope.appeSign() {
	name = ESign.name
	aid = +"A000000167455349474E"
	selectAcl {
		acl(CardProtocol.Grouped.CONTACTLESS) {
			Never
			// For the selection of the eSign-Application there are three options:
			// 1. TAKey with Authentication Terminal (with "Install Qualified Certificate" bit) + CA + PIN
			// 2. TAKey with Signature Terminal (with "Generate qualified electronic signature" bit) + CA + PIN
			// 3. TAKey with Signature Terminal (with "Generate qualified electronic signature" bit) + CA + CAN,
			// 4. TAKey with Signature Terminal (with "Generate qualified electronic signature" bit) + CA + PUK,
			// where CA = CAKey or CAKey-ci.
// 			or(
// 				{
// 					// AT + CAKey + PIN
// 					and(
// 						activeDidState("PIN"),
// 						// "Install Qualified Certificate" bit of an Authentication Terminal
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(+"7F4C12060904007F00070301020253050000000080"),
// 						),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 				{
// 					// ST + CAKey + PIN
// 					and(
// 						activeDidState("PIN"),
// 						// "Generate qualified electronic signature" bit of a Signature Terminal
// 						activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 				{
// 					// ST + CAKey + CAN
// 					and(
// 						activeDidState("CAN"),
// 						// "Generate qualified electronic signature" bit of a Signature Terminal
// 						activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 				{
// 					// ST + CAKey + PUK
// 					and(
// 						activeDidState("PUK"),
// 						// "Generate qualified electronic signature" bit of a Signature Terminal
// 						activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 						activeDidState("CAKey"),
// 					)
// 				},
// 				{
// 					// AT + CAKey-ci + PIN
// 					and(
// 						activeDidState("PIN"),
// 						// "Install Qualified Certificate" bit of an Authentication Terminal
// 						activeDidState(
// 							"TAKey",
// 							PaceAclQualifier(+"7F4C12060904007F00070301020253050000000080"),
// 						),
// 						activeDidState("CAKey-ci"),
// 					)
// 				},
// 				{
// 					// ST + CAKey-ci + PIN
// 					and(
// 						activeDidState("PIN"),
// 						// "Generate qualified electronic signature" bit of a Signature Terminal
// 						activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 						activeDidState("CAKey-ci"),
// 					)
// 				},
// 				{
// 					// ST + CAKey-ci + CAN
// 					and(
// 						activeDidState("CAN"),
// 						// "Generate qualified electronic signature" bit of a Signature Terminal
// 						activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 						activeDidState("CAKey-ci"),
// 					)
// 				},
// 				{
// 					// ST + CAKey-ci + PUK
// 					and(
// 						activeDidState("PUK"),
// 						// "Generate qualified electronic signature" bit of a Signature Terminal
// 						activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 						activeDidState("CAKey-ci"),
// 					)
// 				},
// 			)
		}
	}

	dataSets {
		add {
			name = ESign.Datasets.ef_c_zda_qes
			path = +"C000"
			readAcl {
				pinCanTaKeyCaKeyCiProtectedAcl()
			}

			writeAcl {
				pinTaKeyCaKeyCiProtectedAcl(Mf.Dids.pin, +"7F4C12060904007F00070301020253050000000080")
			}
		}

		add {
			name = ESign.Datasets.ef_c_icc_qes
			path = +"C001"
			readAcl {
				pinCanTaKeyCaKeyCiProtectedAcl()
			}

			writeAcl {
				pinTaKeyCaKeyCiProtectedAcl(Mf.Dids.pin, +"7F4C12060904007F00070301020253050000000080")
			}
		}
	}

	dids {
		signature {
			name = ESign.Dids.prk_icc_qes
			scope = DidScope.LOCAL

			signAcl {
				canTaKeyCaKeyCiProtectedAcl()
			}

			parameters {
				key {
					keyRef = 0x84u
				}
				signatureAlgorithm = "SHA256withECDSA"
				certificates("EF.C.ZDA.QES", "EF.C.ICC.QES")

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

		pin {
			name = ESign.Dids.esignPin
			scope = DidScope.GLOBAL

			authAcl {
				pinTaKeyCaKeyCiProtectedAcl(ESign.Dids.esignPin, +"7F4C12060904007F000703010203530102")
			}

			resetAcl {
				canTaKeyCaKeyCiProtectedAcl()
			}

			parameters {
				passwordRef = 0x81u
				pwdType = PasswordType.ASCII_NUMERIC
				pwdFlags =
					setOf(
						PasswordFlags.RESET_RETRY_COUNTER_WITH_PASSWORD,
						PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK,
					)
				minLength = 6
				maxLength = 6
				storedLength = 6
			}
		}
	}
}
