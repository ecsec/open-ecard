package org.openecard.cif.bundled

import kotlinx.datetime.Instant
import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.definition.meta.CardInfoStatus
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
							Never
						}
					}
				}

				add {
					name = "EF.CardAccess"
					description = "EF.CardAccess is required for the PACE protocol when using the contactless interface."
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

			// TODO: add DIDs
			dids {
				add {
					name = "AUT_PACE"
					scope = DidScope.GLOBAL
				}
			}
		}

		add {
			name = "DF.HCA"
			aid = +"D27600000102"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}
			// TODO: add datasets
			// TODO: add DIDs
		}

		add {
			name = "DF.ESIGN"
			aid = +"A000000167455349474E"
			selectAcl {
				acl(CardProtocol.Any) {
					Always
				}
			}
			// TODO: add datasets
			// TODO: add DIDs
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
			// TODO: add datasets
			// TODO: add DIDs
		}

// 		add {
// 			name = "CIA.ESIGN"
// 			aid = +"E828BD080FA000000167455349474E"
// 			selectAcl {
// 				acl(CardProtocol.Any) {
// 					Always
// 				}
// 			}
// 			// TODO: add datasets
// 			// TODO: add DIDs
// 		}
	}

	b.build()
}
