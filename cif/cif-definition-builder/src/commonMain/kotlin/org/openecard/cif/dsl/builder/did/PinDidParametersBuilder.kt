package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.PasswordFlags
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.definition.did.PinDidParameters
import org.openecard.cif.dsl.api.did.PinDidParametersScope
import org.openecard.cif.dsl.builder.Builder

class PinDidParametersBuilder :
	PinDidParametersScope,
	Builder<PinDidParameters> {
	override var pwdFlags: Set<PasswordFlags> = setOf()
	private var _pwdType: PasswordType? = null
	override var pwdType: PasswordType
		get() = requireNotNull(_pwdType)
		set(value) {
			_pwdType = value
		}
	private var _passwordRef: UByte? = null
	override var passwordRef: UByte
		get() = requireNotNull(_passwordRef)
		set(value) {
			_passwordRef = value
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

	override fun flags(vararg flags: PasswordFlags) {
		pwdFlags = flags.toSet()
	}

	override fun build(): PinDidParameters =
		PinDidParameters(
			pwdFlags = pwdFlags,
			pwdType = pwdType,
			passwordRef = passwordRef,
			minLength = minLength,
			maxLength = maxLength,
			storedLength = storedLength,
			padChar = padChar,
		)
}
