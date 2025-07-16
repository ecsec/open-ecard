package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.definition.did.UnblockingParameters
import org.openecard.cif.dsl.api.did.PinDidPasswordEncodingScope
import org.openecard.cif.dsl.builder.Builder

class PinDidUnblockingParametersBuilder :
	PinDidPasswordEncodingScope,
	Builder<UnblockingParameters> {
	private var _pwdType: PasswordType? = null
	override var pwdType: PasswordType
		get() = requireNotNull(_pwdType)
		set(value) {
			_pwdType = value
		}

	private var _minLength: Int? = null
	override var minLength: Int
		get() = requireNotNull(_minLength)
		set(value) {
			_minLength = value
		}
	override var maxLength: Int? = null

	override var storedLength: Int? = null
	override var padChar: UByte? = null

	override fun build(): UnblockingParameters =
		UnblockingParameters(
			pwdType = pwdType,
			minLength = minLength.toUInt(),
			maxLength = maxLength?.toUInt(),
			storedLength = storedLength?.toUInt(),
			padChar = padChar,
		)
}
