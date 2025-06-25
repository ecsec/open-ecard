package org.openecard.cif.bundled

import kotlinx.datetime.Instant
import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.meta.CardInfoStatus
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
			name = "MF"
			aid = +"3F00"
			selectAcl {
				acl(CardProtocol.Grouped.CONTACTLESS) {
					Always
				}
			}
			// TODO: add datasets
			// TODO: add DIDs
		}

// 		add {
// 			name = "eID"
// 			aid = +"E80704007F00070302"
// 			selectAcl {
// 				acl(CardProtocol.Grouped.CONTACTLESS) {
// 					or(
// 						{
// 							and(
// 								activeDidState("PIN"),
// 								activeDidState("TAKey"),
// 								activeDidState("CAKey"),
// 							)
// 						},
// 						{
// 							and(
// 								activeDidState("CAN"),
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F00070301020253050000000010")),
// 								activeDidState("CAKey"),
// 							)
// 						},
// 					)
// 				}
// 			}
// 			// TODO: add datasets
// 			// TODO: add DIDs
// 		}

// 		add {
// 			name = "eSign"
// 			aid = +"A000000167455349474E"
// 			selectAcl {
// 				acl(CardProtocol.Grouped.CONTACTLESS) {
// 					// For the selection of the eSign-Application there are three options:
// 					// 1. TAKey with Authentication Terminal (with "Install Qualified Certificate" bit) + CA + PIN
// 					// 2. TAKey with Signature Terminal (with "Generate qualified electronic signature" bit) + CA + PIN
// 					// 3. TAKey with Signature Terminal (with "Generate qualified electronic signature" bit) + CA + CAN,
// 					// 4. TAKey with Signature Terminal (with "Generate qualified electronic signature" bit) + CA + PUK,
// 					// where CA = CAKey or CAKey-ci.
// 					or(
// 						{
// 							// AT + CAKey + PIN
// 							and(
// 								activeDidState("PIN"),
// 								// "Install Qualified Certificate" bit of an Authentication Terminal
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F00070301020253050000000080")),
// 								activeDidState("CAKey"),
// 							)
// 						},
// 						{
// 							// ST + CAKey + PIN
// 							and(
// 								activeDidState("PIN"),
// 								// "Generate qualified electronic signature" bit of a Signature Terminal
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 								activeDidState("CAKey"),
// 							)
// 						},
// 						{
// 							// ST + CAKey + CAN
// 							and(
// 								activeDidState("CAN"),
// 								// "Generate qualified electronic signature" bit of a Signature Terminal
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 								activeDidState("CAKey"),
// 							)
// 						},
// 						{
// 							// ST + CAKey + PUK
// 							and(
// 								activeDidState("PUK"),
// 								// "Generate qualified electronic signature" bit of a Signature Terminal
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 								activeDidState("CAKey"),
// 							)
// 						},
// 						{
// 							// AT + CAKey-ci + PIN
// 							and(
// 								activeDidState("PIN"),
// 								// "Install Qualified Certificate" bit of an Authentication Terminal
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F00070301020253050000000080")),
// 								activeDidState("CAKey-ci"),
// 							)
// 						},
// 						{
// 							// ST + CAKey-ci + PIN
// 							and(
// 								activeDidState("PIN"),
// 								// "Generate qualified electronic signature" bit of a Signature Terminal
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 								activeDidState("CAKey-ci"),
// 							)
// 						},
// 						{
// 							// ST + CAKey-ci + CAN
// 							and(
// 								activeDidState("CAN"),
// 								// "Generate qualified electronic signature" bit of a Signature Terminal
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 								activeDidState("CAKey-ci"),
// 							)
// 						},
// 						{
// 							// ST + CAKey-ci + PUK
// 							and(
// 								activeDidState("PUK"),
// 								// "Generate qualified electronic signature" bit of a Signature Terminal
// 								activeDidState("TAKey", PaceAclQualifier(+"7F4C12060904007F000703010203530102")),
// 								activeDidState("CAKey-ci"),
// 							)
// 						},
// 					)
// 				}
// 			}
// 			// TODO: add datasets
// 			// TODO: add DIDs
// 		}
	}

	b.build()
}
