package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.PaceDidParameters
import org.openecard.cif.definition.did.PacePinId
import org.openecard.cif.dsl.api.did.PaceDidParametersScope
import org.openecard.cif.dsl.builder.Builder

class PaceDidParametersBuilder :
	PaceDidParametersScope,
	Builder<PaceDidParameters> {
	private var _passwordRef: PacePinId? = null
	override var passwordRef: PacePinId
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

	override fun build(): PaceDidParameters =
		PaceDidParameters(
			passwordRef = passwordRef,
			minLength = minLength,
			maxLength = maxLength,
		)
}
