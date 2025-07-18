package org.openecard.cif.bundled

import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.PinDidParametersScope
import org.openecard.cif.dsl.api.did.isoPin

object GematikBuildingBlocks {
	val autPace = "AUT_PACE"
	val pinCh = "PIN.CH"

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
			activeDidState(autPace)
		}
	}

	internal fun AclScope.paceCmsProtectedAcl() {
		acl(CardProtocol.Grouped.CONTACT) {
			Always
		}
		acl(CardProtocol.Grouped.CONTACTLESS) {
			or(
				{ activeDidState(autPace) },
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
					activeDidState(autPace)
				},
			)
		}
	}

	internal fun AclScope.pinChPaceProtectedAcl() {
		acl(CardProtocol.Grouped.CONTACT) {
			activeDidState(pinCh)
		}
		acl(CardProtocol.Grouped.CONTACTLESS) {
			and(
				{
					activeDidState(pinCh)
					activeDidState(autPace)
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
				{ activeDidState(autPace) },
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

	internal fun PinDidParametersScope.basePinParams() = isoPin(6, 8)
}
