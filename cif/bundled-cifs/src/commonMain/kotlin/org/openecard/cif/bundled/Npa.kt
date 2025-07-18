package org.openecard.cif.bundled

import kotlinx.datetime.Instant
import org.openecard.cif.bundled.GematikBuildingBlocks.alwaysAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.canPinTaKeyProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.canTaKeyCaKeyCiProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.datasetDG
import org.openecard.cif.bundled.GematikBuildingBlocks.isoPinStandards
import org.openecard.cif.bundled.GematikBuildingBlocks.neverAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.pinCanTaKeyCaKeyCiProtectedAcl
import org.openecard.cif.bundled.GematikBuildingBlocks.pinTaKeyCaKeyCiProtectedAcl
import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.acl.PaceAclQualifier
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.definition.did.PacePinId
import org.openecard.cif.definition.did.PasswordFlags
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.definition.did.SignatureGenerationInfoType
import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.api.application.ApplicationScope
import org.openecard.cif.dsl.builder.CardInfoBuilder
import org.openecard.cif.dsl.builder.unaryPlus

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

private fun ApplicationScope.appMf() {
	name = "MF"
	aid = +"3F00"
	selectAcl {
		acl(CardProtocol.Grouped.CONTACTLESS) {
			Always
		}
	}

	dataSets {
		add {
			name = "EF.DIR"
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
			name = "EF.ATR"
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
			name = "EF.CardAccess"
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
			name = "EF.CardSecurity"
			path = +"011D"
			readAcl {
				canPinTaKeyProtectedAcl(null, null)
			}
			writeAcl {
				neverAcl()
			}
		}

		add {
			name = "EF.ChipSecurity"
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
			name = "PIN"
			scope = DidScope.GLOBAL

			authAcl {
				alwaysAcl()
			}

			resetAcl {
				acl(CardProtocol.Any) {
					activeDidState("PIN")
				}
			}

			parameters {
				passwordRef = 0x03u
				pwdType = PasswordType.ASCII_NUMERIC
				pwdFlags =
					setOf(
						PasswordFlags.RESET_RETRY_COUNTER_WITH_PASSWORD,
						PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK,
					)
				isoPinStandards()
			}
		}

		pace {
			name = "PACE_PIN"
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
			name = "CAN"
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
			name = "PUK"
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
	name = "eID"
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
				name = "DG1",
				path = +"0101",
				+"7F4C12060904007F00070301020253050000000100",
			)
		}

		add {
			datasetDG(
				name = "DG2",
				path = +"0102",
				+"7F4C12060904007F00070301020253050000000200",
			)
		}

		add {
			datasetDG(
				name = "DG3",
				path = +"0103",
				+"7F4C12060904007F00070301020253050000004000",
			)
		}

		add {
			datasetDG(
				name = "DG4",
				path = +"0104",
				+"7F4C12060904007F00070301020253050000000800",
			)
		}

		add {
			datasetDG(
				name = "DG5",
				path = +"0105",
				+"7F4C12060904007F00070301020253050000001000",
			)
		}

		add {
			datasetDG(
				name = "DG6",
				path = +"0106",
				+"7F4C12060904007F00070301020253050000002000",
			)
		}

		add {
			datasetDG(
				name = "DG7",
				path = +"0107",
				+"7F4C12060904007F00070301020253050000004000",
			)
		}

		add {
			datasetDG(
				name = "DG8",
				path = +"0108",
				+"7F4C12060904007F00070301020253050000008000",
			)
		}

		add {
			datasetDG(
				name = "DG9",
				path = +"0109",
				+"7F4C12060904007F00070301020253050000010000",
			)
		}

		add {
			datasetDG(
				name = "DG10",
				path = +"010A",
				+"7F4C12060904007F00070301020253050000020000",
			)
		}

		add {
			datasetDG(
				name = "DG11",
				path = +"010B",
				+"7F4C12060904007F00070301020253050000040000",
			)
		}

		add {
			datasetDG(
				name = "DG17",
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
	name = "eSign"
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
			name = "EF.C.ZDA.QES"
			path = +"C000"
			readAcl {
				pinCanTaKeyCaKeyCiProtectedAcl()
			}

			writeAcl {
				pinTaKeyCaKeyCiProtectedAcl("PIN", +"7F4C12060904007F00070301020253050000000080")
			}
		}

		add {
			name = "EF.C.ICC.QES"
			path = +"C001"
			readAcl {
				pinCanTaKeyCaKeyCiProtectedAcl()
			}

			writeAcl {
				pinTaKeyCaKeyCiProtectedAcl("PIN", +"7F4C12060904007F00070301020253050000000080")
			}
		}
	}

	dids {
		signature {
			name = "PrK.ICC.QES"
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
			name = "eSign-PIN"
			scope = DidScope.GLOBAL

			authAcl {
				pinTaKeyCaKeyCiProtectedAcl("eSign-PIN", +"7F4C12060904007F000703010203530102")
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
