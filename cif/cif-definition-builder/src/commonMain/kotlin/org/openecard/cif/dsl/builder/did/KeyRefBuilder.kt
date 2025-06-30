package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.KeyRefDefinition
import org.openecard.cif.dsl.api.did.KeyRefScope
import org.openecard.cif.dsl.builder.Builder

class KeyRefBuilder :
	KeyRefScope,
	Builder<KeyRefDefinition> {
	private var _keyRef: UByte? = null
	override var keyRef: UByte
		get() = requireNotNull(_keyRef)
		set(value) {
			_keyRef = value
		}
	override var keySize: Int? = null
	override var nonceSize: Int? = null

	override fun build(): KeyRefDefinition =
		KeyRefDefinition(
			keyRef = keyRef,
			keySize = keySize,
			nonceSize = nonceSize,
		)
}
