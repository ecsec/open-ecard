package org.openecard.cif.bundled

import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.did.PinDidParametersScope

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

	internal fun PinDidParametersScope.basePinParams() {
		pwdFlags = setOf()
		pwdType = PasswordType.ISO_9564_1
		minLength = 6
		maxLength = 8
		storedLength = 8
		padChar = 0xFFu
	}
}
