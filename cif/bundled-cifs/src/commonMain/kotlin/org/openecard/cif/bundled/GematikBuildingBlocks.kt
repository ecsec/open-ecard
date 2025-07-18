package org.openecard.cif.bundled

import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.acl.PaceAclQualifier
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.dataset.DataSetScope
import org.openecard.cif.dsl.api.did.PinDidParametersScope
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.cif.dsl.api.did.isoPin

object GematikBuildingBlocks {
	internal fun AclScope.alwaysAcl() {
		acl(CardProtocol.Any) {
			Always
		}
	}

	internal fun AclScope.neverAcl() {
		acl(CardProtocol.Any) {
			Never
		}
	}

	internal fun AclScope.paceProtectedAcl() {
		acl(CardProtocol.Grouped.CONTACT) {
			Always
		}
		acl(CardProtocol.Grouped.CONTACTLESS) {
			activeDidState("AUT_PACE")
		}
	}

	internal fun AclScope.paceCmsProtectedAcl() {
		acl(CardProtocol.Grouped.CONTACT) {
			Always
		}
		acl(CardProtocol.Grouped.CONTACTLESS) {
			or(
				{ activeDidState("AUT_PACE") },
				// { activeDidState("AUT_CMS") },
			)
		}
	}

	internal fun AclScope.cmsProtectedAcl() {
		acl(CardProtocol.Any) {
			Never
			// { activeDidState("AUT_CMS") },
		}
	}

	internal fun AclScope.pinProtectedPaceAcl(pin: String) {
		acl(CardProtocol.Grouped.CONTACT) {
			activeDidState(pin)
		}
		acl(CardProtocol.Grouped.CONTACTLESS) {
			and(
				{
					activeDidState(pin)
					activeDidState("AUT_PACE")
				},
			)
		}
	}

	internal fun AclScope.mrPinHomePaceProtectedAcl() {
		acl(CardProtocol.Grouped.CONTACT) {
			activeDidState("MRPIN.home")
		}
		acl(CardProtocol.Grouped.CONTACTLESS) {
			and(
				{
					activeDidState("MRPIN.home")
					activeDidState("AUT_PACE")
				},
			)
		}
	}

	internal fun AclScope.pinChPaceProtectedAcl() {
		acl(CardProtocol.Grouped.CONTACT) {
			activeDidState("PIN.CH")
		}
		acl(CardProtocol.Grouped.CONTACTLESS) {
			and(
				{
					activeDidState("PIN.CH")
					activeDidState("AUT_PACE")
				},
			)
		}
	}

	internal fun AclScope.paceCmsCupProtectedAcl() {
		acl(CardProtocol.Grouped.CONTACT) {
			Always
		}
		acl(CardProtocol.Grouped.CONTACTLESS) {
			or(
				{ activeDidState("AUT_PACE") },
				// { activeDidState("AUT_CMS") },
				// { activeDidState("AUT_CUP") },
			)
		}
	}

	internal fun AclScope.cmsCupProtectedAcl() {
		acl(CardProtocol.Any) {
			Never
// 				or(
// 					 { activeDidState("AUT_CMS") },
// 					 { activeDidState("AUT_CUP") },
// 				)
		}
	}

	internal fun AclScope.pinProtectedAcl() {
		acl(CardProtocol.Any) {
			activeDidState("PIN")
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

	internal fun PinDidParametersScope.isoPinStandards() {
		minLength = 6
		maxLength = 8
		storedLength = 8
		padChar = 0xFFu
	}
	internal fun PinDidParametersScope.basePinParams() = isoPin(6, 8)
}
