package org.openecard.cif.dsl.api.did

import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.dsl.api.CifScope

interface PinDidPasswordEncodingScope : CifScope {
	var pwdType: PasswordType
	var minLength: Int
	var maxLength: Int?
	var storedLength: Int?
	var padChar: UByte?
}

fun PinDidPasswordEncodingScope.isoPuk(
	minLength: Int,
	maxLength: Int,
) {
	pwdType = PasswordType.ISO_9564_1
	this.minLength = minLength
	this.maxLength = maxLength
	storedLength = 8
	padChar = 0xFFu
}
