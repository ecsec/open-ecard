package org.openecard.cif.dsl.api.did

import org.openecard.cif.definition.did.PasswordFlags
import org.openecard.cif.definition.did.UnblockingParameters
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.CifScope

interface PinDidParametersScope :
	CifScope,
	PinDidPasswordEncodingScope {
	var pwdFlags: Set<PasswordFlags>
	var passwordRef: UByte
	var unblockingPassword: UnblockingParameters?

	fun flags(vararg flags: PasswordFlags)

	fun unblockingPassword(content: @CifMarker PinDidPasswordEncodingScope.() -> Unit)
}
