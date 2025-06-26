package org.openecard.cif.dsl.api.did

import org.openecard.cif.definition.did.PasswordFlags
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.dsl.api.CifScope

interface PinDidParametersScope : CifScope {
	var pwdFlags: Set<PasswordFlags>
	var pwdType: PasswordType
	var passwordRef: UByte
	var minLength: Int
	var maxLength: Int?
	var storedLength: Int?
	var padChar: UByte?

	fun flags(vararg flags: PasswordFlags)
}
